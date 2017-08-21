package com.abstractplanner.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.abstractplanner.R;

public final class AbstractPlannerPreferences {

    public static final int VERSION_CODE_DOESNT_EXIST = -1;
    private static final String PREF_VERSION_CODE = "version_code";
    private static final String PREF_IS_DATABASE_INITIAL_STATUS = "initial_data_only";
    private static final String PREF_IS_USER_AUTHORIZED = "is_user_authorized";

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

    // Set every day remind notification to set tasks for tomorrow enabled
    public static void setNotificationEnabled(Context context, boolean enabled){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putBoolean(context.getString(R.string.tomorrow_tasks_notification_key), enabled).apply();
    }

    // Is every day remind notification to set tasks for tomorrow enabled
    public static boolean isNotificationEnabled(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(context.getString(R.string.tomorrow_tasks_notification_key), true);
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
