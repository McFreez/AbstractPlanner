package com.abstractplanner.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.abstractplanner.R;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.receivers.AlarmReceiver;

public final class NotificationUtils {

    public static void deleteNotification(Context context, Notification notification){
        Intent alarmIntent = new Intent(context, AlarmReceiver.class);
        alarmIntent.putExtra("message", notification.getMessage());
        if(notification.getTask() != null) {
            if(notification.getTask().getType() == Task.TYPE_QUICK)
                alarmIntent.putExtra("title", context.getString(R.string.quick_task_title));
            else
                alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        }
        else
            alarmIntent.putExtra("title", Notification.getNotificationTypeName(notification.getType()) + " notification");
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }
}
