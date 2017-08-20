package com.abstractplanner.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import com.abstractplanner.LoginActivity;

public final class AbstractPlannerPreferences {

    private static final String IS_USER_AUTHORIZED = "is_user_authorized";

    public static void setUserAuthorized(Context context, boolean isAuthorized){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putBoolean(IS_USER_AUTHORIZED, isAuthorized).apply();
    }

    public static boolean isUserAuthorized(Context context){
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(IS_USER_AUTHORIZED, false);
    }
}
