package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.receivers.AlarmReceiver;

import java.util.Calendar;

public class AddTaskNotificationDialogFragment extends DialogFragment {

    private EventListener mEventListener;

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

        LinearLayout mNotifyIn30Min = (LinearLayout) view.findViewById(R.id.task_notify_in_30_min);
        mNotifyIn30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                int minutes = 30;

                createNotification(minutes);
            }
        });
        LinearLayout mNotifyIn1Hour = (LinearLayout) view.findViewById(R.id.task_notify_in_1_hour);
        mNotifyIn1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                int minutes = 60;

                createNotification(minutes);
            }
        });
        LinearLayout mNotifyIn3Hours = (LinearLayout) view.findViewById(R.id.task_notify_in_3_hours);
        mNotifyIn3Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                int minutes = 3 * 60;

                createNotification(minutes);
            }
        });
        LinearLayout mNotifyIn6Hours = (LinearLayout) view.findViewById(R.id.task_notify_in_6_hours);
        mNotifyIn6Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mTaskToNotify == null) {
                    dismiss();
                    return;
                }

                int minutes = 6 * 60;

                createNotification(minutes);
            }
        });
        LinearLayout mSelectExactTime = (LinearLayout) view.findViewById(R.id.task_select_exact_time);
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

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // The only reason you might override this method when using onCreateView() is
        // to modify any dialog characteristics. For example, the dialog includes a
        // title by default, but your custom layout might not need it. So here you can
        // remove the dialog title, but you must call the superclass to get the Dialog.

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    private void createNotification(int minute){
        Calendar notificationTime = Calendar.getInstance();
        notificationTime.add(Calendar.MINUTE, minute);

        Notification notification = new Notification("You have unfinished task: " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

        long id = mDbHelper.createNotification(notification);

        if(id > 0){
            notification.setId(id);
            createNotification(notification);
        }

        dismiss();
    }

    private void createNotification(int hourOfDay, int minute){
        Calendar now = Calendar.getInstance();
        Calendar notificationTime = Calendar.getInstance();

        notificationTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
        notificationTime.set(Calendar.MINUTE, minute);

        if(now.after(notificationTime))
            notificationTime.add(Calendar.DATE, 1);

        Notification notification = new Notification("You have unfinished task: " + mTaskToNotify.getName(), notificationTime, mTaskToNotify, Notification.TYPE_ONE_TIME_ID);

        long id = mDbHelper.createNotification(notification);

        if(id > 0){
            notification.setId(id);
            createNotification(notification);
        }

        dismiss();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(mEventListener != null)
            mEventListener.onDismissed();

        this.dismiss();
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
            createNotification(hourOfDay, minute);
        }
    };

    private void setTime() {
        Calendar notificationTime = Calendar.getInstance();
        //notificationTime.set(mTaskToNotify.getDate().get(Calendar.YEAR), mTaskToNotify.getDate().get(Calendar.MONTH), mTaskToNotify.getDate().get(Calendar.DAY_OF_MONTH));

        new TimePickerDialog(getContext(), s,
                notificationTime.get(Calendar.HOUR_OF_DAY),
                notificationTime.get(Calendar.MINUTE),
                true)
                .show();
    }

    private void createNotification(Notification notification){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);

        alarmIntent.putExtra("message", notification.getMessage());

        if(notification.getTask().getType() == Task.TYPE_QUICK){
            alarmIntent.putExtra("title", getString(R.string.quick_task_title));
        } else
            alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        alarmIntent.putExtra("id", notification.getId());

        Long idLong = notification.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        switch (notification.getType()){
            case Notification.TYPE_ONE_TIME_ID:
                if(today.before(notification.getDate())) {
                    if (Build.VERSION.SDK_INT >= 19)
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
        }
    }

    public void setTask(Task task){
        mTaskToNotify = task;
    }

    public void setEventListener(EventListener eventListener){
        mEventListener = eventListener;
    }
}
