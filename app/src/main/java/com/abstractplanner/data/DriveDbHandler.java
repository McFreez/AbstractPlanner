package com.abstractplanner.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.metadata.CustomPropertyKey;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public final class DriveDbHandler {

    private static final String LOG_TAG = "DriveDbHandler";

    private static final String PACKAGE_NAME = "com.abstractplanner";

    private static final String DATABASE_PATH =
            "/data/data/" + PACKAGE_NAME + "/databases/" + AbstractPlannerDatabaseHelper.DATABASE_NAME;

    private static final String FILE_NAME = AbstractPlannerDatabaseHelper.DATABASE_NAME;
    private static final String MIME_TYPE = "application/x-sqlite-3";

    private static final String JUST_CREATED_PROPERTY = "is_just_created";

    private DriveDbHandler() {
    }


    public static void tryCreatingDbOnDrive(final GoogleApiClient googleApiClient, final Context context) {
        // We need to check if the database already exists on Google Drive. If so, we won't create
        // it again.

        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.TITLE, FILE_NAME),
                        Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE)))
                .build();

        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);

        appFolder.queryChildren(googleApiClient, query).setResultCallback(
                new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(LOG_TAG, "Query for " + FILE_NAME + " unsuccessful!");
                            return;
                        }

                        int count = metadataBufferResult.getMetadataBuffer().getCount();

                        Log.d(LOG_TAG, "Successfully ran query for " + FILE_NAME + " and found " +
                                count + " results");

                        if (count > 1) {
                            Log.e(LOG_TAG, "App folder contains more than one database file! " +
                                    "Found " + count + " matching results.");


                            metadataBufferResult.release();
                            return;
                        }

                        // Create the database on Google Drive if it doesn't exist already
                        if (count == 0) {
                            Log.d(LOG_TAG, "No existing database found on Google Drive");
                            saveToDrive(googleApiClient, context);
                        }
                        metadataBufferResult.release();
                    }
                });
    }

    private static void saveToDrive(final GoogleApiClient googleApiClient, final Context context) {
        Log.d(LOG_TAG, "Starting to save to drive...");

        // Create content from file
        Drive.DriveApi.newDriveContents(googleApiClient).setResultCallback(
                new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.DriveContentsResult driveContentsResult) {
                        if (!driveContentsResult.getStatus().isSuccess()) {
                            Log.w(LOG_TAG, "Drive contents result not a success! " +
                                    "With message: " + driveContentsResult.getStatus().getStatusMessage());
                            return;
                        }

                        Log.d(LOG_TAG, "Created drive contents for file");
                        createNewFile(googleApiClient, driveContentsResult.getDriveContents(), context);
                    }
                });
    }

    private static void createNewFile(GoogleApiClient googleApiClient, DriveContents driveContents, Context context) {
        // Write file to contents (see http://stackoverflow.com/a/33610727/4230345)
        //File file = new File(DATABASE_PATH);
        OutputStream outputStream = driveContents.getOutputStream();
        try {
            InputStream inputStream = new FileInputStream(DATABASE_PATH);
            byte[] buf = new byte[1024];
            int length;
            while ((length = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, length);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(LOG_TAG, "Written file to output stream of drive contents");

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean isDbInitial = prefs.getBoolean(AbstractPlannerDatabaseHelper.PREF_IS_DATABASE_INITIAL_STATUS, true);

        int status;
        if(isDbInitial)
            status = 1;
        else
            status = 0;

        // Create metadata
        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                .setTitle(FILE_NAME)
                .setMimeType(MIME_TYPE)
                .setCustomProperty(new CustomPropertyKey(JUST_CREATED_PROPERTY, CustomPropertyKey.PUBLIC), String.valueOf(status))
                .build();

        // Create the file on Google Drive
        DriveFolder folder = Drive.DriveApi.getAppFolder(googleApiClient);
        folder.createFile(googleApiClient, metadataChangeSet, driveContents).setResultCallback(
                new ResultCallback<DriveFolder.DriveFileResult>() {
                    @Override
                    public void onResult(@NonNull DriveFolder.DriveFileResult driveFileResult) {
                        if (!driveFileResult.getStatus().isSuccess()) {
                            Log.w(LOG_TAG, "File did not get created in Google Drive!"
                                + " With message: " + driveFileResult.getStatus().getStatusMessage());
                            return;
                        }

                        Log.i(LOG_TAG, "Successfully created file in Google Drive");
                    }
                });
    }

    public static void tryReadDbFromDrive(final GoogleApiClient googleApiClient, final Context context){
        Query query = new Query.Builder()
                .addFilter(Filters.and(
                        Filters.eq(SearchableField.TITLE, FILE_NAME),
                        Filters.eq(SearchableField.MIME_TYPE, MIME_TYPE)))
                .build();

        DriveFolder appFolder = Drive.DriveApi.getAppFolder(googleApiClient);

        appFolder.queryChildren(googleApiClient, query).setResultCallback(
                new ResultCallback<DriveApi.MetadataBufferResult>() {
                    @Override
                    public void onResult(@NonNull DriveApi.MetadataBufferResult metadataBufferResult) {
                        if (!metadataBufferResult.getStatus().isSuccess()) {
                            Log.e(LOG_TAG, "Query for " + FILE_NAME + " unsuccessful!");
                            return;
                        }

                        int count = metadataBufferResult.getMetadataBuffer().getCount();

                        Log.d(LOG_TAG, "Successfully ran query for " + FILE_NAME + " and found " +
                                count + " results");

                        if (count > 1) {
                            Log.e(LOG_TAG, "App folder contains more than one database file! " +
                                    "Found " + count + " matching results.");

                            List<Integer> removeIds = new ArrayList<Integer>();

                            for(int i = 0; i < count; i++){
                                Log.e(LOG_TAG, (i + 1) + " data title: " + metadataBufferResult.getMetadataBuffer().get(i).getTitle() + "\n"
                                        + " creation date: " + metadataBufferResult.getMetadataBuffer().get(i).getCreatedDate());
                                if(metadataBufferResult.getMetadataBuffer().get(i).getCustomProperties().containsKey(JUST_CREATED_PROPERTY) && metadataBufferResult.getMetadataBuffer().get(i).getCustomProperties().get(JUST_CREATED_PROPERTY).equals(String.valueOf(1)))
                                    removeIds.add(i);
                            }

                            if(removeIds.size() == count){
                                removeIds.remove(0);
                            }

                            if(removeIds.size() > 0) {
                                for (int i = removeIds.size(); i >= 0; i--) {
                                    // TODO: USE ASYNC TASK
                                    DriveFile driveFile = metadataBufferResult.getMetadataBuffer().get(removeIds.get(i)).getDriveId().asDriveFile();
                                    driveFile.delete(googleApiClient);
                                }
                            }

                            int notInitialDBsCount = metadataBufferResult.getMetadataBuffer().getCount();

                            if(notInitialDBsCount > 1) {
                                for (int i = 1; i < notInitialDBsCount; i++) {
                                    // TODO: USE ASYNC TASK
                                    DriveFile driveFile = metadataBufferResult.getMetadataBuffer().get(i).getDriveId().asDriveFile();
                                    driveFile.delete(googleApiClient);
                                }
                            }
                        }

                        if(count > 0){
                            readDbFromDrive(googleApiClient, metadataBufferResult.getMetadataBuffer().get(0).getDriveId().asDriveFile());
                            metadataBufferResult.release();
                            return;
                        }

                        // Create the database on Google Drive if it doesn't exist already
                        if (count == 0) {
                            Log.d(LOG_TAG, "No existing database found on Google Drive");
                            saveToDrive(googleApiClient, context);
                        }
                        metadataBufferResult.release();
                    }
                });
    }

    private static void readDbFromDrive(final GoogleApiClient googleApiClient, DriveFile file){
        file.open(googleApiClient, DriveFile.MODE_READ_ONLY, null)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                            @Override
                            public void onResult(DriveApi.DriveContentsResult result) {
                                if (!result.getStatus().isSuccess()) {
                                    // display an error saying file can't be opened
                                    Log.w(LOG_TAG, "Drive contents result not a success! " +
                                            "With message: " + result.getStatus().getStatusMessage());
                                    return;
                                }
                                // DriveContents object contains pointers
                                // to the actual byte stream
                                Log.d(LOG_TAG, "Successfully opened file from Google Drive");
                                DriveContents contents = result.getDriveContents();
                                try {
                                    InputStream inputStream = contents.getInputStream();
                                    OutputStream outputStream = new FileOutputStream(DATABASE_PATH);
                                    byte[] buf = new byte[1024];
                                    int length;
                                    while ((length = inputStream.read(buf)) > 0) {
                                        outputStream.write(buf, 0, length);
                                    }
                                    outputStream.flush();
                                    outputStream.close();
                                    inputStream.close();

                                    Log.d(LOG_TAG, "Database copied successfully");

                                }catch (IOException e){
                                    e.printStackTrace();
                                }
                                contents.discard(googleApiClient);

                                Log.d(LOG_TAG, "Reading database from Google Drive succeed");
                            }
                        });
    }
}
