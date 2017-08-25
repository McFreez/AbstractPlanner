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
import android.os.Parcelable;
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

public class RescheduleNotificationDialogFragment extends DialogFragment {

    private EventListener mEventListener;

    private LinearLayout mDeferFor30Min;
    private LinearLayout mDeferFor1Hour;
    private LinearLayout mDeferFor3Hours;
    private LinearLayout mDeferFor6Hours;
    private LinearLayout mSelectExactTime;

    private AbstractPlannerDatabaseHelper mDbHelper;

    private Notification mNotificationToDefer;

    public interface EventListener extends Parcelable{
        void onNotificationDefered();

        void onDismissed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_reschedule_notification, null);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        TextView m30MinTime = (TextView) view.findViewById(R.id.notification_defer_for_30_min_time);
        TextView m1HourTime = (TextView) view.findViewById(R.id.notification_defer_for_1_hour_time);
        TextView m3HoursTime = (TextView) view.findViewById(R.id.notification_defer_for_3_hours_time);
        TextView m6HoursTime = (TextView) view.findViewById(R.id.notification_defer_for_6_hours_time);

        Calendar notificationTime = Calendar.getInstance();

        if(mNotificationToDefer != null) {
            notificationTime.setTimeInMillis(mNotificationToDefer.getDate().getTimeInMillis());

            notificationTime.add(Calendar.MINUTE, 30);
            m30MinTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

            notificationTime.add(Calendar.MINUTE, 30);
            m1HourTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

            notificationTime.add(Calendar.MINUTE, 120);
            m3HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

            notificationTime.add(Calendar.MINUTE, 180);
            m6HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));
        }

        mDeferFor30Min = (LinearLayout) view.findViewById(R.id.notification_defer_for_30_min);
        mDeferFor30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
                    dismiss();
                    return;
                }

                int minutes = 30;

                updateNotificationData(minutes);
            }
        });
        mDeferFor1Hour = (LinearLayout) view.findViewById(R.id.notification_defer_for_1_hour);
        mDeferFor1Hour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
                    dismiss();
                    return;
                }

                int minutes = 60;

                updateNotificationData(minutes);
            }
        });
        mDeferFor3Hours = (LinearLayout) view.findViewById(R.id.notification_defer_for_3_hours);
        mDeferFor3Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
                    dismiss();
                    return;
                }

                int minutes = 3 * 60;

                updateNotificationData(minutes);
            }
        });
        mDeferFor6Hours = (LinearLayout) view.findViewById(R.id.notification_defer_for_6_hours);
        mDeferFor6Hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
                    dismiss();
                    return;
                }

                int minutes = 6 * 60;

                updateNotificationData(minutes);

            }
        });
        mSelectExactTime = (LinearLayout) view.findViewById(R.id.notification_select_exact_time);
        mSelectExactTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
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

    private void updateNotificationData(int minute){
        mNotificationToDefer.getDate().add(Calendar.MINUTE, minute);

        long status = mDbHelper.updateNotification(mNotificationToDefer);

        if(status > 0){
            updateNotification();
        }

        if(mEventListener != null)
            mEventListener.onNotificationDefered();

        dismiss();
    }

    private void updateNotificationData(int hourOfDay, int minute){
        Calendar now = Calendar.getInstance();

        mNotificationToDefer.getDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
        mNotificationToDefer.getDate().set(Calendar.MINUTE, minute);

        if(now.after(mNotificationToDefer.getDate()))
            mNotificationToDefer.getDate().add(Calendar.DATE, 1);

        long status = mDbHelper.updateNotification(mNotificationToDefer);

        if(status > 0){
            updateNotification();
        }

        if(mEventListener != null)
            mEventListener.onNotificationDefered();

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
            updateNotificationData(hourOfDay, minute);
        }
    };

    private void setTime() {
        new TimePickerDialog(getContext(), s,
                mNotificationToDefer.getDate().get(Calendar.HOUR_OF_DAY),
                mNotificationToDefer.getDate().get(Calendar.MINUTE),
                true)
                .show();
    }

    private void updateNotification(){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);

        if(mNotificationToDefer != null) {
            alarmIntent.putExtra("message", mNotificationToDefer.getMessage());
            if(mNotificationToDefer.getTask() != null) {
                if(mNotificationToDefer.getTask().getType() == Task.TYPE_QUICK)
                    alarmIntent.putExtra("title", getString(R.string.quick_task_title));
                else
                    alarmIntent.putExtra("title", mNotificationToDefer.getTask().getArea().getName());
            }
            else
                if(mNotificationToDefer.getType() == Notification.TYPE_SYSTEM_ID)
                    alarmIntent.putExtra("title", "Remind");
                else
                    alarmIntent.putExtra("title", Notification.getNotificationTypeName(mNotificationToDefer.getType()) + " notification");
            alarmIntent.putExtra("id", mNotificationToDefer.getId());

            Long previousIdLong = mNotificationToDefer.getId();
            int previousId = previousIdLong.intValue();

            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getContext(), previousId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(previousPendingIntent);

            alarmIntent.getExtras().clear();
        }

        alarmIntent.putExtra("message", mNotificationToDefer.getMessage());
        if(mNotificationToDefer.getTask() != null) {
            if(mNotificationToDefer.getTask().getType() == Task.TYPE_QUICK){
                alarmIntent.putExtra("title", getString(R.string.quick_task_title));
            } else
                alarmIntent.putExtra("title", mNotificationToDefer.getTask().getArea().getName());
        }
        else
            if(mNotificationToDefer.getType() == Notification.TYPE_SYSTEM_ID)
                alarmIntent.putExtra("title", "Remind");
            else
                alarmIntent.putExtra("title", Notification.getNotificationTypeName(mNotificationToDefer.getType()) + " notification");
        alarmIntent.putExtra("id", mNotificationToDefer.getId());

        Long idLong = mNotificationToDefer.getId();
        int id = idLong.intValue();

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        switch (mNotificationToDefer.getType()){
            case Notification.TYPE_ONE_TIME_ID:
                if(today.before(mNotificationToDefer.getDate())) {
                    if (Build.VERSION.SDK_INT >= 19)
                        manager.setExact(AlarmManager.RTC_WAKEUP, mNotificationToDefer.getDate().getTimeInMillis(), pendingIntent);
                    else
                        manager.set(AlarmManager.RTC_WAKEUP, mNotificationToDefer.getDate().getTimeInMillis(), pendingIntent);
                }
                break;
            case Notification.TYPE_EVERY_DAY_ID:

                Calendar notificationDate = mNotificationToDefer.getDate();
                notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notificationDate))
                    notificationDate.add(Calendar.DATE, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
            case Notification.TYPE_SYSTEM_ID:
                Calendar notifDate = mNotificationToDefer.getDate();
                notifDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notifDate))
                    notifDate.add(Calendar.DATE, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notifDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
                break;
        }
    }

    public void setNotification(Notification notification){
        mNotificationToDefer = notification;
    }

    public void setEventListener(EventListener eventListener){
        mEventListener = eventListener;
    }
}
