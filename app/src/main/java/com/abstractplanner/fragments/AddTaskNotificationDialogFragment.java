package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.recievers.AlarmReceiver;

import java.util.Calendar;

public class AddTaskNotificationDialogFragment extends DialogFragment {

    private EventListener mEventListener;

    private LinearLayout mNotifyIn30Min;
    private LinearLayout mNotifyIn1Hour;
    private LinearLayout mNotifyIn3Hours;
    private LinearLayout mNotifyIn6Hours;
    private LinearLayout mSelectExactTime;

    private AbstractPlannerDatabaseHelper mDbHelper;

    private Task mTaskToNotify;

    public interface EventListener{
        void onDismissed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_task_notification, null);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        Calendar notificationTime = Calendar.getInstance();
        //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));

        notificationTime.add(Calendar.MINUTE, 30);
        TextView m30MinTime = (TextView) view.findViewById(R.id.task_notify_in_30_min_time);
        m30MinTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 30);
        TextView m1HourTime = (TextView) view.findViewById(R.id.task_notify_in_1_hour_time);
        m1HourTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 120);
        TextView m3HoursTime = (TextView) view.findViewById(R.id.task_notify_in_3_hours_time);
        m3HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 180);
        TextView m6HoursTime = (TextView) view.findViewById(R.id.task_notify_in_6_hours_time);
        m6HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        mNotifyIn30Min = (LinearLayout) view.findViewById(R.id.task_notify_in_30_min);
        mNotifyIn30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                Calendar notificationTime = Calendar.getInstance();
                //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));
                notificationTime.add(Calendar.MINUTE, 30);

                Notification notification = new Notification("You have unfinished task " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

                long id = mDbHelper.createNotification(notification);

                if(id > 0){
                    notification.setId(id);
                    createNotification(notification);
                }

                dismiss();
            }
        });
        mNotifyIn1Hour = (LinearLayout) view.findViewById(R.id.task_notify_in_1_hour);
        mNotifyIn1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                Calendar notificationTime = Calendar.getInstance();
                //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));
                notificationTime.add(Calendar.HOUR, 1);

                Notification notification = new Notification("You have unfinished task " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

                long id = mDbHelper.createNotification(notification);

                if(id > 0){
                    notification.setId(id);
                    createNotification(notification);
                }

                dismiss();
            }
        });
        mNotifyIn3Hours = (LinearLayout) view.findViewById(R.id.task_notify_in_3_hours);
        mNotifyIn3Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                Calendar notificationTime = Calendar.getInstance();
                //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));
                notificationTime.add(Calendar.HOUR, 3);

                Notification notification = new Notification("You have unfinished task " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

                long id = mDbHelper.createNotification(notification);

                if(id > 0){
                    notification.setId(id);
                    createNotification(notification);
                }

                dismiss();
            }
        });
        mNotifyIn6Hours = (LinearLayout) view.findViewById(R.id.task_notify_in_6_hours);
        mNotifyIn6Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                Calendar notificationTime = Calendar.getInstance();
                //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));
                notificationTime.add(Calendar.HOUR, 6);

                Notification notification = new Notification("You have unfinished task " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

                long id = mDbHelper.createNotification(notification);

                if(id > 0){
                    notification.setId(id);
                    createNotification(notification);
                }

                dismiss();
            }
        });
        mSelectExactTime = (LinearLayout) view.findViewById(R.id.task_select_exact_time);
        mSelectExactTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                setTime();
            }
        });

        return view;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mEventListener != null)
            mEventListener.onDismissed();

        super.onDismiss(dialog);
    }

    TimePickerDialog.OnTimeSetListener s = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {

            Calendar notificationTime = Calendar.getInstance();
            //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));

            notificationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            notificationTime.set(Calendar.MINUTE, minute);

            Notification notification = new Notification("You have unfinished task: " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

            long id = mDbHelper.createNotification(notification);

            if(id > 0){
                notification.setId(id);
                createNotification(notification);
            }

            dismiss();
        }
    };

    private void setTime() {
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));

        new TimePickerDialog(getContext(), s,
                notificationTime.get(Calendar.HOUR_OF_DAY),
                notificationTime.get(Calendar.MINUTE),
                true)
                .show();
    }

    private void createNotification(Notification notification){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);
/*
        if(mNotificationToDefer != null) {
            alarmIntent.putExtra("message", mNotificationToDefer.getMessage());
            alarmIntent.putExtra("title", notification.getTask().getArea().getName());

            Long previousIdLong = mNotificationToDefer.getId();
            int previousId = previousIdLong.intValue();
            alarmIntent.putExtra("id", mNotificationToDefer.getId());

            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getContext(), previousId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(previousPendingIntent);

            alarmIntent.getExtras().clear();
        }*/

        alarmIntent.putExtra("message", notification.getMessage());
        alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        switch (notification.getType()){
            case Notification.TYPE_ONE_TIME_ID:
                if(today.before(notification.getDate()))
                    manager.setExact(AlarmManager.RTC_WAKEUP, notification.getDate().getTimeInMillis(), pendingIntent);
                break;
            case Notification.TYPE_EVERY_DAY_ID:

                Calendar notificationDate = notification.getDate();
                notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notificationDate))
                    notificationDate.add(Calendar.DAY_OF_MONTH, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void setTask(Task task){
        mTaskToNotify = task;
    }

    public void setEventListener(EventListener eventListener){
        mEventListener = eventListener;
    }
}
