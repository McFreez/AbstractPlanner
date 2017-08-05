package com.abstractplanner.recievers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;

public class AlarmReceiver extends BroadcastReceiver{

    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        sendNotification(intent.getStringExtra("message"), intent.getStringExtra("title"), intent.getLongExtra("id", 0));
    }

    private void sendNotification(String messageBody, String messageTitle, long id){

        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0 /*Request code */, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_event_note_white_24dp)
/*                .setLargeIcon(largeIcon())*/
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Long idLong = id;
        int intId = idLong.intValue();

        notificationManager.notify(intId/* ID of notification */, notificationBuilder.build());

        AbstractPlannerDatabaseHelper dbHelper = new AbstractPlannerDatabaseHelper(mContext);

        com.abstractplanner.dto.Notification notification = dbHelper.getNotificationByID(id);
        if(notification != null)
            if(notification.getType() == com.abstractplanner.dto.Notification.TYPE_ONE_TIME_ID)
                dbHelper.deleteNotification(notification.getId());
    }

    private Bitmap largeIcon() {
        Resources res = mContext.getResources();
        Bitmap largeIcon = BitmapFactory.decodeResource(res, R.drawable.ic_event_note_white_24dp);
        return largeIcon;
    }
}
