
package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.util.Log;
import android.widget.Toast;

import com.abstractplanner.R;
import com.abstractplanner.SettingsActivity;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.data.AbstractPlannerPreferences;
import com.abstractplanner.data.DataXmlExporter;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.receivers.AlarmReceiver;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Calendar;

import static android.app.Activity.RESULT_OK;

/**
 * The SettingsFragment serves as the display for all of the user's settings. In Sunshine, the
 * user will be able to change their preference for units of measurement from metric to imperial,
 * set their preferred weather location, and indicate whether or not they'd like to see
 * notifications.
 *
 * Please note: If you are using our dummy weather services, the location returned will always be
 * Mountain View, California.
 */
public class SettingsFragment extends PreferenceFragmentCompat implements
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String LOG_TAG = "SettingsFragment";

    private Preference mSignInButton;

    private AbstractPlannerDatabaseHelper mDbHelper;

    protected static final int REQUEST_CODE_CREATOR = 1;

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            /* For list preferences, look up the correct display value in */
            /* the preference's 'entries' list (since they have separate labels/values). */
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                if(key.equals(getString(R.string.pref_areas_sort_key)))
                    preference.setSummary("By " + listPreference.getEntries()[prefIndex]);
                else
                    preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CREATOR:
                if (resultCode == RESULT_OK) {
                    /*DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);*/
                    Toast.makeText(getContext(), "File created successfully", Toast.LENGTH_LONG).show();
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    final ResultCallback<DriveApi.DriveContentsResult> driveContentsCallback =
            new ResultCallback<DriveApi.DriveContentsResult>() {
                @Override
                public void onResult(DriveApi.DriveContentsResult result) {

                    String dbName = getString(R.string.app_name) + "_database";

                    OutputStream outputStream = result.getDriveContents().getOutputStream();
                    try {
                        String dbInXml = new DataXmlExporter(mDbHelper.getReadableDatabase()).getDBContentsInXml(dbName);
                        outputStream.write(dbInXml.getBytes(Charset.forName("UTF-8")));
                    }
                    catch (IOException e){
                        Log.e(LOG_TAG, "Unable to open database: " + e.getMessage());
                        Toast.makeText(getContext(), "Unable to open database", Toast.LENGTH_LONG).show();
                        return;
                    }

                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                            .setTitle(dbName + ".xml")
                            .setMimeType("text/xml").build();

                    IntentSender intentSender = Drive.DriveApi
                            .newCreateFileActivityBuilder()
                            .setInitialMetadata(metadataChangeSet)
                            .setInitialDriveContents(result.getDriveContents())
                            .build(((SettingsActivity) getActivity()).getGoogleApiClient());
                    try {
                        startIntentSenderForResult(intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0, null);
                    } catch (IntentSender.SendIntentException e) {
                        Log.w(LOG_TAG, "Unable to send intent", e);
                    }
                }
            };

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        /* Add 'general' preferences, defined in the XML file */
        addPreferencesFromResource(R.xml.pref_general);

        mDbHelper = new AbstractPlannerDatabaseHelper(getContext());

        mSignInButton = findPreference(getString(R.string.pref_sign_in_key));
        updateSignInButtonPreference(false);

        Preference exportButton = findPreference(getString(R.string.pref_export_key));
        exportButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                if(!((SettingsActivity) getActivity()).isUserAuthorized()){
                    Toast.makeText(getContext(), "Sign in first",Toast.LENGTH_SHORT).show();
                    return false;
                }

                Drive.DriveApi.newDriveContents(((SettingsActivity) getActivity()).getGoogleApiClient())
                        .setResultCallback(driveContentsCallback);

                return true;
            }
        });

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            PreferenceCategory category = (PreferenceCategory) prefScreen.getPreference(i);

            for(int j = 0; j < category.getPreferenceCount(); j++) {
                Preference p = category.getPreference(j);
                if (p instanceof SwitchPreferenceCompat) {
                    //String value = sharedPreferences.getString(p.getKey(), "");
                    //setPreferenceSummary(p, value);
                    if (p.getKey().equals(getString(R.string.pref_tomorrow_tasks_notification_key))) {
                        boolean isNotificationEnabled = sharedPreferences.getBoolean(p.getKey(), true);

                        Notification notification = mDbHelper.getNotificationByMessageAndType(getString(R.string.pref_tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

                        if (notification == null && isNotificationEnabled) {
                            notification = mDbHelper.createSystemNotification(getString(R.string.pref_tomorrow_tasks_notification_message));
                            if (notification == null)
                                return;
                        }

                        if (isNotificationEnabled) {
                            createNotification(notification);
                        } else
                            clearNotification(notification);
                    } else
                        if(p.getKey().equals(getString(R.string.pref_unfinished_quick_tasks_key))){
                            boolean isNotificationEnabled = sharedPreferences.getBoolean(p.getKey(), true);

                            Notification notification = mDbHelper.getNotificationByMessageAndType(getString(R.string.pref_unfinished_quick_tasks_message), Notification.TYPE_SYSTEM_ID);

                            if (notification == null && isNotificationEnabled) {
                                notification = mDbHelper.createSystemNotification(getString(R.string.pref_unfinished_quick_tasks_message));
                                if (notification == null)
                                    return;
                            }

                            if (isNotificationEnabled) {
                                createNotification(notification);
                            } else
                                clearNotification(notification);
                        }
                } else if (p instanceof ListPreference) {
                    String value = sharedPreferences.getString(p.getKey(), "");
                    setPreferenceSummary(p, value);
/*                    ListPreference listPreference = (ListPreference) p;
                    int prefIndex = listPreference.findIndexOfValue(sharedPreferences.getString(p.getKey(),""));
                    if (prefIndex >= 0) {
                        p.setSummary(listPreference.getEntries()[prefIndex]);
                    }*/
                }
            }
        }

        //dbHelper.close();
    }

    private void updateSignInButtonPreference(boolean enableSignIn){
        if(AbstractPlannerPreferences.isAuthorizationEnabled(getContext())){
            mSignInButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    ((SettingsActivity) getActivity()).signIn();

                    return true;
                }
            });
            mSignInButton.setEnabled(true);

            if(enableSignIn)
                ((SettingsActivity) getActivity()).signIn();

        } else {
            mSignInButton.setOnPreferenceClickListener(null);
            mSignInButton.setEnabled(false);
        }

    }

    private void createNotification(Notification notification){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        alarmIntent.putExtra("title", "Remind");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        Calendar notificationDate = notification.getDate();
        notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        if (today.after(notificationDate))
            notificationDate.add(Calendar.DATE, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void clearNotification(Notification notification){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        alarmIntent.putExtra("title", "Remind");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.cancel(pendingIntent);
    }

    @Override
    public void onStop() {
        super.onStop();
        /* Unregister the preference change listener */
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        /* Register the preference change listener */
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);

        if(preference != null){
            if(preference instanceof ListPreference){
                setPreferenceSummary(preference, sharedPreferences.getString(key, ""));
            } else
                if(preference instanceof SwitchPreferenceCompat){
                    if (preference.getKey().equals(getString(R.string.pref_tomorrow_tasks_notification_key))) {
                        //AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

                        boolean isNotificationEnabled = sharedPreferences.getBoolean(key, true);

                        Notification notification = mDbHelper.getNotificationByMessageAndType(getString(R.string.pref_tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

                        if (notification == null && isNotificationEnabled) {
                            notification = mDbHelper.createSystemNotification(getString(R.string.pref_tomorrow_tasks_notification_message));
                            if (notification == null)
                                return;
                        }

                        if (isNotificationEnabled)
                            createNotification(notification);
                        else
                            clearNotification(notification);

                        //dbHelper.close();
                    } else
                        if (preference.getKey().equals(getString(R.string.pref_unfinished_quick_tasks_key))) {
                            //AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

                            boolean isNotificationEnabled = sharedPreferences.getBoolean(key, true);

                            Notification notification = mDbHelper.getNotificationByMessageAndType(getString(R.string.pref_unfinished_quick_tasks_message), Notification.TYPE_SYSTEM_ID);

                            if (notification == null && isNotificationEnabled) {
                                notification = mDbHelper.createSystemNotification(getString(R.string.pref_unfinished_quick_tasks_message));
                                if (notification == null)
                                    return;
                            }

                            if (isNotificationEnabled)
                                createNotification(notification);
                            else
                                clearNotification(notification);

                            //dbHelper.close();
                        }
            }

            if(key.equals(getString(R.string.pref_google_authorization_key))){
                updateSignInButtonPreference(true);
            }
        }
    }
}