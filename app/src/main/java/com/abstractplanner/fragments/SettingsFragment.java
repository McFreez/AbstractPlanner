
package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.receivers.AlarmReceiver;

import java.util.Calendar;

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

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            /* For list preferences, look up the correct display value in */
            /* the preference's 'entries' list (since they have separate labels/values). */
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            // For other preferences, set the summary to the value's simple string representation.
            preference.setSummary(stringValue);
        }
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        /* Add 'general' preferences, defined in the XML file */
        addPreferencesFromResource(R.xml.pref_general);

        AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        PreferenceScreen prefScreen = getPreferenceScreen();
        int count = prefScreen.getPreferenceCount();
        for (int i = 0; i < count; i++) {
            Preference p = prefScreen.getPreference(i);
            if (p instanceof SwitchPreferenceCompat) {
                //String value = sharedPreferences.getString(p.getKey(), "");
                //setPreferenceSummary(p, value);
                if(p.getKey().equals(getString(R.string.tomorrow_tasks_notification_key))){
                    boolean isNotificationEnabled = sharedPreferences.getBoolean(p.getKey(), true);

                    Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

                    if(notification == null && isNotificationEnabled){
                        notification = dbHelper.createSystemNotification(getString(R.string.tomorrow_tasks_notification_message));
                        if(notification == null)
                            return;
                    }

                    if(isNotificationEnabled){
                        createNotification(notification);
                    }
                    else
                        clearNotification(notification);
                }
            }
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

        AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

        boolean isNotificationEnabled = sharedPreferences.getBoolean(key, true);

        Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

        if(notification == null && isNotificationEnabled){
            notification = dbHelper.createSystemNotification(getString(R.string.tomorrow_tasks_notification_message));
            if(notification == null)
                return;
        }

        if(isNotificationEnabled)
            createNotification(notification);
        else
            clearNotification(notification);
    }
}