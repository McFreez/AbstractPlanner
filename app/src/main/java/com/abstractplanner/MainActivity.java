package com.abstractplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.abstractplanner.data.AbstractDataProvider;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.data.AbstractPlannerPreferences;
import com.abstractplanner.data.DriveDbHandler;
import com.abstractplanner.data.NotificationsDataProvider;
import com.abstractplanner.data.NotificationsDataProviderFragment;
import com.abstractplanner.data.TasksDataProvider;
import com.abstractplanner.data.TasksDataProviderFragment;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.AddAreaFragment;
import com.abstractplanner.fragments.AddTaskFragment;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.fragments.NotificationsFragment;
import com.abstractplanner.fragments.TodayTasksFragment;
import com.abstractplanner.receivers.AlarmReceiver;
import com.abstractplanner.utils.DateTimeUtils;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveFolder;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks{

    private static final String LOG_TAG = "MainActivity";

    private static final int REQUEST_CODE_RESOLUTION = 1;

    private AbstractPlannerDatabaseHelper dbHelper;

    private DrawerLayout mDrawer;
    private Toolbar mLongToolbar;
    private NavigationView mNavigationView;
    private TextView mUserName;
    private TextView mUserEmail;
    private ImageView mUserImage;

    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInAccount mAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongToolbar = (Toolbar) findViewById(R.id.toolbar_long);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        changeSupportActionBar(mLongToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        View header = mNavigationView.getHeaderView(0);

        mUserName = (TextView) header.findViewById(R.id.user_google_name);
        mUserEmail = (TextView) header.findViewById(R.id.user_google_email);
        mUserImage = (ImageView) header.findViewById(R.id.user_google_image);

        dbHelper = new AbstractPlannerDatabaseHelper(this);

        if(savedInstanceState == null) {
            checkFirstRun();
            Intent intentThatRunsThisActivity = getIntent();
            if(intentThatRunsThisActivity != null && intentThatRunsThisActivity.getExtras() != null && intentThatRunsThisActivity.getExtras().containsKey("showCalendar"))
                displaySelectedScreen(R.id.calendar_grid, null);
            else
                displaySelectedScreen(R.id.today_tasks, null);
        }

        PreferenceManager.getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);

        if(AbstractPlannerPreferences.isUserAuthorized(this)) {
            if (mGoogleApiClient == null) {
                // Configure sign-in to request the user's ID, email address, and basic
                // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                        .build();

                // Build a GoogleApiClient with access to the Google Sign-In API and the
                // options specified by gso.
                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .addApi(Drive.API)
/*                    .addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)*/
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
            //mGoogleApiClient.connect();
        }
    }

    private void checkFirstRun() {

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        int savedVersionCode = AbstractPlannerPreferences.getAppVersionCode(this);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == AbstractPlannerPreferences.VERSION_CODE_DOESNT_EXIST) {

            if(!AbstractPlannerPreferences.isNotificationEnabled(this))
                AbstractPlannerPreferences.setNotificationEnabled(this, true);

            createSystemNotification();

            Area sport = new Area("Sport", "My sport tasks");
            Area english = new Area("English", "English learning");

            long sportAreaID = dbHelper.createArea(sport);
            long englishAreaID = dbHelper.createArea(english);

            if(sportAreaID >= 0)
                sport.setId(sportAreaID);

            if(englishAreaID >= 0)
                english.setId(englishAreaID);

            Calendar taskDate = DateTimeUtils.getTodayDate();

            taskDate.add(Calendar.DATE, -1);

            Task gym = new Task(sport, "Gym", "Go to gym at 18:00", taskDate, true);
            Task learnText = new Task(english, "Learn text", "Learn big text", taskDate, false);

            long gymId = dbHelper.createTask(gym);
            long learnTextId = dbHelper.createTask(learnText);

            taskDate.add(Calendar.DATE, 1);

            Task newEx = new Task(sport, "New exercises", "Find new exercises", taskDate, false);
            Task newWords = new Task(english, "New words", "Learn 10 new words", taskDate, false);

            long newExID = dbHelper.createTask(newEx);
            long newWordsID = dbHelper.createTask(newWords);

            dbHelper.setDbInitialStatus();

        } else if (currentVersionCode > savedVersionCode) {
            if(!AbstractPlannerPreferences.isNotificationEnabled(this))
                AbstractPlannerPreferences.setNotificationEnabled(this, true);

            createSystemNotification();
        }

        // Update the shared preferences with the current version code
        AbstractPlannerPreferences.setAppVersionCode(this, currentVersionCode);
    }

    private void createSystemNotification(){

        Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

        if(notification == null){
            notification = dbHelper.createSystemNotification(getString(R.string.tomorrow_tasks_notification_message));
            if(notification == null)
                return;
        }

        AlarmManager manager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        alarmIntent.putExtra("title", "Remind");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        Calendar notificationDate = notification.getDate();
        notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        if (today.after(notificationDate))
            notificationDate.add(Calendar.DATE, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if(AbstractPlannerPreferences.isUserAuthorized(this)) {
            OptionalPendingResult<GoogleSignInResult> opr = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
            if (opr.isDone()) {
                // If the user's cached credentials are valid, the OptionalPendingResult will be "done"
                // and the GoogleSignInResult will be available instantly.
                Log.d(LOG_TAG, "Got cached sign-in");
                GoogleSignInResult result = opr.get();
                handleSignInResult(result);
            } else {
                // If the user has not previously signed in on this device or the sign-in has expired,
                // this asynchronous branch will attempt to sign in the user silently.  Cross-device
                // single sign-on will occur in this branch.
                opr.setResultCallback(new ResultCallback<GoogleSignInResult>() {
                    @Override
                    public void onResult(GoogleSignInResult googleSignInResult) {
                        handleSignInResult(googleSignInResult);
                    }
                });
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(AbstractPlannerPreferences.isUserAuthorized(this)) {
            if (mGoogleApiClient == null) {
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestEmail()
                        .requestScopes(new Scope(Scopes.DRIVE_APPFOLDER))
                        .build();

                mGoogleApiClient = new GoogleApiClient.Builder(this)
                        .enableAutoManage(this, this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .addApi(Drive.API)
                    /*.addScope(Drive.SCOPE_FILE)
                    .addScope(Drive.SCOPE_APPFOLDER)*/
                        .addConnectionCallbacks(this)
                        .addOnConnectionFailedListener(this)
                        .build();
            }
            //mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onPause() {
        if(AbstractPlannerPreferences.isUserAuthorized(this)) {
            if (mGoogleApiClient != null) {
                mGoogleApiClient.disconnect();
            }
        }
        super.onPause();
    }

    private void handleSignInResult(GoogleSignInResult result){
        if (result.isSuccess()) {
            // Google Sign In was successful
            mAccount = result.getSignInAccount();
            updateUserData();
        } else {
            // Google Sign In failed, update UI appropriately
            // [START_EXCLUDE]
            mAccount = null;
            signOut();
            // [END_EXCLUDE]
        }
    }

    private void updateUserData(){
        if(mAccount != null){
            mUserName.setText(mAccount.getDisplayName());
            mUserEmail.setText(mAccount.getEmail());
            Uri photoUri = mAccount.getPhotoUrl();
            if(photoUri != null)
                new GetUserImageTask().execute(photoUri);
            else {
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_user_no_image);
                RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(getResources(), bitmap);
                roundedBitmapDrawable.setCircular(true);

                if(android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    mUserImage.setBackground(roundedBitmapDrawable);
                else
                    mUserImage.setBackgroundDrawable(roundedBitmapDrawable);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        /*
         * Set this flag to true so that when control returns to MainActivity, it can refresh the
         * data.
         *
         * This isn't the ideal solution because there really isn't a need to perform another
         * GET request just to change the units, but this is the simplest solution that gets the
         * job done for now. Later in this course, we are going to show you more elegant ways to
         * handle converting the units from celsius to fahrenheit and back without hitting the
         * network again by keeping a copy of the data in a manageable format.
         */
        //PREFERENCES_HAVE_BEEN_UPDATED = true;
    }

    @Override
    public void setTitle(CharSequence title) {
        super.setTitle(title);

        mLongToolbar.setTitle(title);
    }

    private void changeSupportActionBar(Toolbar toolbar){
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close){
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

            }
        };
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } /*else {
            super.onBackPressed();
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(startSettingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displaySelectedScreen(int id, Map<String, Object> additionalData){
        Fragment fragment = null;

        switch (id){
            case R.id.today_tasks:
                fragment = new TodayTasksFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(new TasksDataProviderFragment(), TasksDataProvider.PROVIDER_ID)
                        .commit();
                break;
            case R.id.calendar_grid:
                fragment = new CalendarGridFragment();
                break;
            case R.id.add_area:
                fragment = new AddAreaFragment();
                break;
            case R.id.add_task:
                fragment = new AddTaskFragment();
                if(additionalData != null){
                    if(additionalData.containsKey("taskDay") && additionalData.containsKey("taskAreaName")) {
                        Calendar date = (Calendar) additionalData.get("taskDay");
                        String areaName = (String) additionalData.get("taskAreaName");

                        ((AddTaskFragment)fragment).setPredefinedParameters(areaName, date);
                    }
                }
                break;
            case R.id.notifications_list:
                fragment = new NotificationsFragment();
                getSupportFragmentManager().beginTransaction()
                        .add(new NotificationsDataProviderFragment(), NotificationsDataProvider.PROVIDER_ID)
                        .commit();
                break;
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                break;
            case R.id.action_exit:
                signOut();
                break;
        }

        if(id != R.id.action_settings) {
            mNavigationView.setCheckedItem(id);
        }

        if(fragment != null){
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_main, fragment);
            ft.commit();
        }
    }

    public void setShortAppBar(Toolbar shortToolbar){
        mLongToolbar.setVisibility(View.GONE);
        shortToolbar.setVisibility(View.VISIBLE);

        changeSupportActionBar(shortToolbar);

    }

    public void setLongAppBar(Toolbar shortToolbar){
        shortToolbar.setVisibility(View.GONE);
        mLongToolbar.setVisibility(View.VISIBLE);

        changeSupportActionBar(mLongToolbar);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        displaySelectedScreen(item.getItemId(), null);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return false;
    }

    public AbstractDataProvider getDataProvider(String providerID) {

        final Fragment fragment = getSupportFragmentManager().findFragmentByTag(providerID);

        switch (providerID){
            case TasksDataProvider.PROVIDER_ID:
                return ((TasksDataProviderFragment) fragment).getDataProvider();
            case NotificationsDataProvider.PROVIDER_ID:
                return ((NotificationsDataProviderFragment) fragment).getDataProvider();
            default:
                return null;

        }
    }

    public AbstractPlannerDatabaseHelper getDbHelper(){
        return dbHelper;
    }

    private void signOut() {
        Log.i(LOG_TAG, "Disconnection from google account");

        if(AbstractPlannerPreferences.isUserAuthorized(this)) {
            // Google sign out
            Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(
                    new ResultCallback<Status>() {
                        @Override
                        public void onResult(@NonNull Status status) {
                            AbstractPlannerPreferences.setUserAuthorized(MainActivity.this, false);
                            MainActivity.this.finish();
                        }
                    });
        }
        else
            finish();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.i(LOG_TAG, "GoogleApiClient connection has been failed");
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(LOG_TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.i(LOG_TAG, "GoogleApiClient connected");

        try {
            getDbHelper().close();
            /*DriveDbHandler.TryReadDBTask tryReadDBTask = new DriveDbHandler.TryReadDBTask(mGoogleApiClient, this);
            tryReadDBTask.execute();*/
            //DriveDbHandler.tryReadDbFromDrive(mGoogleApiClient, this);
            //displaySelectedScreen(R.id.today_tasks, null);
        } catch (IllegalStateException e){
            Log.e(LOG_TAG, "Cannot connect to Google Drive " + e.getMessage());
            signOut();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(LOG_TAG, "GoogleApiClient connection suspended");
    }

    /**
     * Handles resolution callbacks.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_RESOLUTION && resultCode == RESULT_OK) {
            mGoogleApiClient.connect();
        }
    }

    public class GetUserImageTask extends AsyncTask<Uri, Void, Bitmap> {

        @Override
        protected Bitmap doInBackground(Uri... params) {
            try {
                URL url = new URL(params[0].toString());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setDoInput(true);
                connection.connect();
                InputStream input = connection.getInputStream();
                Bitmap myBitmap = BitmapFactory.decodeStream(input);
                return myBitmap;
            } catch (IOException e) {
                // Log exception
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            mUserImage.setImageBitmap(bitmap);
        }
    }
}