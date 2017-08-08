package com.abstractplanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.abstractplanner.data.AbstractDataProvider;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.data.NotificationsDataProvider;
import com.abstractplanner.data.NotificationsDataProviderFragment;
import com.abstractplanner.data.TasksDataProvider;
import com.abstractplanner.data.TasksDataProviderFragment;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.fragments.AddAreaFragment;
import com.abstractplanner.fragments.AddTaskFragment;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.fragments.NotificationsFragment;
import com.abstractplanner.fragments.TodayTasksFragment;
import com.abstractplanner.receivers.AlarmReceiver;

import java.util.Calendar;
import java.util.Map;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SharedPreferences.OnSharedPreferenceChangeListener{

    private AbstractPlannerDatabaseHelper dbHelper;

    private DrawerLayout mDrawer;
    private Toolbar mLongToolbar;
    private NavigationView mNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLongToolbar = (Toolbar) findViewById(R.id.toolbar_long);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        changeSupportActionBar(mLongToolbar);

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

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
    }

    private void checkFirstRun() {

        //final String PREFS_NAME = "MyPrefsFile";
        final String PREF_VERSION_CODE_KEY = "version_code";
        final int DOESNT_EXIST = -1;

        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        int savedVersionCode = prefs.getInt(PREF_VERSION_CODE_KEY, DOESNT_EXIST);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return;

        } else if (savedVersionCode == DOESNT_EXIST) {

            boolean isNotificationEnabled = prefs.getBoolean(getString(R.string.tomorrow_tasks_notification_key), true);
            if(!isNotificationEnabled)
                prefs.edit().putBoolean(getString(R.string.tomorrow_tasks_notification_key), true).apply();

            createSystemNotification();

        } else if (currentVersionCode > savedVersionCode) {
            boolean isNotificationEnabled = prefs.getBoolean(getString(R.string.tomorrow_tasks_notification_key), true);
            if(!isNotificationEnabled)
                prefs.edit().putBoolean(getString(R.string.tomorrow_tasks_notification_key), true).apply();

            createSystemNotification();
        }

        // Update the shared preferences with the current version code
        prefs.edit().putInt(PREF_VERSION_CODE_KEY, currentVersionCode).apply();
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
        } else {
            super.onBackPressed();
        }
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
                /*if(additionalData != null){
                    if(additionalData.containsKey("taskDay") && additionalData.containsKey("taskAreaName")) {
                        Calendar date = (Calendar) additionalData.get("taskDay");
                        String areaName = (String) additionalData.get("taskAreaName");

                        ((AddTaskFragment)fragment).setPredefinedParameters(areaName, date);
                    }
                }*/
                break;
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                break;
        }

        if(id != R.id.action_settings)
            mNavigationView.getMenu().findItem(id).setChecked(true);

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

        return true;
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

}