package com.abstractplanner.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.abstractplanner.BuildConfig;
import com.abstractplanner.R;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.receivers.AlarmReceiver;
import com.abstractplanner.utils.DateTimeUtils;

import java.util.Calendar;

public final class AbstractPlannerPreferences {

    public static final int VERSION_CODE_DOESNT_EXIST = -1;
    private static final String PREF_VERSION_CODE = "version_code";
    private static final String PREF_IS_DATABASE_INITIAL_STATUS = "initial_data_only";
    private static final String PREF_IS_USER_AUTHORIZED = "is_user_authorized";
    //private static final String PREF_IS_AUTHORIZATION_ENABLED = "is_authorization_enabled";

    // Set version of application
    public static void setAppVersionCode(Context context, int currentVersionCode){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(PREF_VERSION_CODE, currentVersionCode).apply();
    }

    // Get application version
    public static int getAppVersionCode(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getInt(PREF_VERSION_CODE, VERSION_CODE_DOESNT_EXIST);
    }

    // Check if app runs first time
    public static boolean checkFirstRun(Context context, AbstractPlannerDatabaseHelper dbHelper) {

        boolean result = false;
        // Get current version code
        int currentVersionCode = BuildConfig.VERSION_CODE;

        // Get saved version code
        int savedVersionCode = AbstractPlannerPreferences.getAppVersionCode(context);

        // Check for first run or upgrade
        if (currentVersionCode == savedVersionCode) {

            // This is just a normal run
            return result;

        } else if (savedVersionCode == AbstractPlannerPreferences.VERSION_CODE_DOESNT_EXIST) {

            if(!isNotificationEnabled(context))
                setNotificationEnabled(context, true);

            createSystemNotification(context, dbHelper);

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

            result = true;

        } else if (currentVersionCode > savedVersionCode) {
            if(!isNotificationEnabled(context))
                setNotificationEnabled(context, true);

            createSystemNotification(context, dbHelper);

            // TODO: fill db with initial data if db is empty
        }

        // Update the shared preferences with the current version code
        setAppVersionCode(context, currentVersionCode);

        return result;
    }

    // Create system notification on first run
    private static void createSystemNotification(Context context, AbstractPlannerDatabaseHelper dbHelper){

        Notification notification = dbHelper.getNotificationByMessageAndType(context.getString(R.string.pref_tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

        if(notification == null){
            notification = dbHelper.createSystemNotification(context.getString(R.string.pref_tomorrow_tasks_notification_message));
            if(notification == null)
                return;
        }

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(context, AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        alarmIntent.putExtra("title", "Remind");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        Calendar notificationDate = notification.getDate();
        notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

        if (today.after(notificationDate))
            notificationDate.add(Calendar.DATE, 1);

        manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    // Set every day remind notification to set tasks for tomorrow enabled
    public static void setNotificationEnabled(Context context, boolean enabled){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(context.getString(R.string.pref_tomorrow_tasks_notification_key), enabled).apply();
    }

    // Is every day remind notification to set tasks for tomorrow enabled
    public static boolean isNotificationEnabled(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.pref_tomorrow_tasks_notification_key), true);
    }

    // Define user authorization with Google account status
    public static void setUserAuthorized(Context context, boolean isAuthorized){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_IS_USER_AUTHORIZED, isAuthorized).apply();
    }

    // Check if user is authorized with Google account
    public static boolean isUserAuthorized(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_IS_USER_AUTHORIZED, false);
    }

    public static void setAuthorizationEnabled(Context context, boolean enabled){
        final String key = context.getString(R.string.pref_google_authorization_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(key, enabled).apply();
    }

    public static boolean isAuthorizationEnabled(Context context){
        final String key = context.getString(R.string.pref_google_authorization_key);
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, true);
    }

    // Set database status to "Initial data only"
    public static void setDatabaseInitialStatus(Context context, boolean enabled){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(PREF_IS_DATABASE_INITIAL_STATUS, enabled).apply();
    }

    // Check if database contains only initial data
    public static boolean isDatabaseInInitialStatus(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(PREF_IS_DATABASE_INITIAL_STATUS, false);
    }
}
