
package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.SwitchPreferenceCompat;

import com.abstractplanner.R;
import com.abstractplanner.SettingsActivity;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.data.AbstractPlannerPreferences;
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

    private Preference mSignInButton;

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
    public void onCreatePreferences(Bundle bundle, String s) {
        /* Add 'general' preferences, defined in the XML file */
        addPreferencesFromResource(R.xml.pref_general);

        mSignInButton = findPreference(getString(R.string.pref_sign_in_key));
        updateSignInButtonPreference(false);

        AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

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

                        Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.pref_tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

                        if (notification == null && isNotificationEnabled) {
                            notification = dbHelper.createSystemNotification(getString(R.string.pref_tomorrow_tasks_notification_message));
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

                            Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.pref_unfinished_quick_tasks_message), Notification.TYPE_SYSTEM_ID);

                            if (notification == null && isNotificationEnabled) {
                                notification = dbHelper.createSystemNotification(getString(R.string.pref_unfinished_quick_tasks_message));
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
                        AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

                        boolean isNotificationEnabled = sharedPreferences.getBoolean(key, true);

                        Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.pref_tomorrow_tasks_notification_message), Notification.TYPE_SYSTEM_ID);

                        if (notification == null && isNotificationEnabled) {
                            notification = dbHelper.createSystemNotification(getString(R.string.pref_tomorrow_tasks_notification_message));
                            if (notification == null)
                                return;
                        }

                        if (isNotificationEnabled)
                            createNotification(notification);
                        else
                            clearNotification(notification);

                        dbHelper.close();
                    } else
                        if (preference.getKey().equals(getString(R.string.pref_unfinished_quick_tasks_key))) {
                            AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(getContext());

                            boolean isNotificationEnabled = sharedPreferences.getBoolean(key, true);

                            Notification notification = dbHelper.getNotificationByMessageAndType(getString(R.string.pref_unfinished_quick_tasks_message), Notification.TYPE_SYSTEM_ID);

                            if (notification == null && isNotificationEnabled) {
                                notification = dbHelper.createSystemNotification(getString(R.string.pref_unfinished_quick_tasks_message));
                                if (notification == null)
                                    return;
                            }

                            if (isNotificationEnabled)
                                createNotification(notification);
                            else
                                clearNotification(notification);

                            dbHelper.close();
                        }
            }

            if(key.equals(getString(R.string.pref_google_authorization_key))){
                updateSignInButtonPreference(true);
            }
        }
    }
}