package com.abstractplanner.receivers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v7.preference.PreferenceManager;
import android.text.format.DateUtils;
import android.util.Log;

import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerContract;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.TimeZone;


public class DeviceBootReceiver extends BroadcastReceiver {

    private Context mContext;

    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        //Log.e("ABSTRACT_PLANNER", " received ");
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            //Log.e("ABSTRACT_PLANNER", " started ");
            AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(mContext);

            //Log.e("ABSTRACT_PLANNER", "db null");

            Cursor notificationsCursor = dbHelper.getAllNotifications();

            if(notificationsCursor == null) {
                //Log.e("ABSTRACT_PLANNER", " cursor null ");
                return;
            }

            if(notificationsCursor.getCount() <= 0){
                //Log.e("ABSTRACT_PLANNER", " size 0 ");
                return;
            }

            Calendar today = Calendar.getInstance();

            for(int i = 0; i < notificationsCursor.getCount(); i++){
                notificationsCursor.moveToPosition(i);

                //Log.e("ABSTRACT_PLANNER", " i " + i);

                Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_DATE)),
                        TimeZone.getTimeZone(notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TIME_ZONE))));

                long notificationID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry._ID));
                int notificationType = notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE));

                if(notificationType == Notification.TYPE_ONE_TIME_ID){
                    Log.e("ABSTRACT_PLANNER", " notification NOT deleted. Today: "
                            + DateUtils.formatDateTime(mContext, today.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR)
                            + " notification datetime: "
                            + DateUtils.formatDateTime(mContext, notificationDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR)
                    );
                }

                String message = notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_MESSAGE));

                if(notificationType == Notification.TYPE_SYSTEM_ID) {
                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean isNotificationEnabled = false;

                    if (message.equals(mContext.getString(R.string.pref_tomorrow_tasks_notification_message))) {
                        isNotificationEnabled = sharedPreferences.getBoolean(mContext.getString(R.string.pref_tomorrow_tasks_notification_key), true);
                    }

                    if (!isNotificationEnabled)
                        continue;
                }

                //Log.e("ABSTRACT_PLANNER", "Type: " + Notification.getNotificationTypeName(notificationType) + " and message: " + message);

                long taskID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TASK_ID));
                Task task = dbHelper.getTaskByID(taskID);

                Notification notification = new Notification(notificationID,
                        message,
                        notificationDate,
                        task,
                        notificationType);

                if(notificationType == Notification.TYPE_ONE_TIME_ID && today.after(notificationDate)){
                    clearNotification(notification);
                    dbHelper.deleteNotification(notificationID);
                    //Log.e("ABSTRACT_PLANNER", " notification deleted ");
                    continue;
                }

                createNotification(notification);

                //Log.e("ABSTRACT_PLANNER", " notification created ");

            }
        }
    }

    private void createNotification(Notification notification){
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        if(notification.getTask() != null)
            alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        else
            if(notification.getType() == Notification.TYPE_SYSTEM_ID)
                alarmIntent.putExtra("title", "Remind");
            else
                alarmIntent.putExtra("title", Notification.getNotificationTypeName(notification.getType()) + " notification");

        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        switch (notification.getType()){
            case Notification.TYPE_ONE_TIME_ID:
                if(today.before(notification.getDate())) {
                    if(Build.VERSION.SDK_INT >= 19)
                        manager.setExact(AlarmManager.RTC_WAKEUP, notification.getDate().getTimeInMillis(), pendingIntent);
                    else
                        manager.set(AlarmManager.RTC_WAKEUP, notification.getDate().getTimeInMillis(), pendingIntent);
                }
                break;
            case Notification.TYPE_EVERY_DAY_ID:

                Calendar notificationDate = notification.getDate();
                notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notificationDate))
                    notificationDate.add(Calendar.DATE, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case Notification.TYPE_SYSTEM_ID:

                Calendar notifDate = notification.getDate();
                notifDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notifDate))
                    notifDate.add(Calendar.DATE, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notifDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
        }
    }

    private void clearNotification(Notification notification){
        AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());
        if(notification.getTask() != null)
            alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        else
            if(notification.getType() == Notification.TYPE_SYSTEM_ID)
                alarmIntent.putExtra("title", "Remind");
            else
                alarmIntent.putExtra("title", Notification.getNotificationTypeName(notification.getType()) + " notification");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        manager.cancel(pendingIntent);
    }

}
