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
import com.abstractplanner.recievers.AlarmReceiver;

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

    public interface EventListener{
        void onNotificationDefered();

        void onDismissed();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_reschedule_notification, null);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        Calendar notificationTime = Calendar.getInstance();
        notificationTime.setTimeInMillis(mNotificationToDefer.getDate().getTimeInMillis());

        notificationTime.add(Calendar.MINUTE, 30);
        TextView m30MinTime = (TextView) view.findViewById(R.id.notification_defer_for_30_min_time);
        m30MinTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 30);
        TextView m1HourTime = (TextView) view.findViewById(R.id.notification_defer_for_1_hour_time);
        m1HourTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 120);
        TextView m3HoursTime = (TextView) view.findViewById(R.id.notification_defer_for_3_hours_time);
        m3HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        notificationTime.add(Calendar.MINUTE, 180);
        TextView m6HoursTime = (TextView) view.findViewById(R.id.notification_defer_for_6_hours_time);
        m6HoursTime.setText(DateUtils.formatDateTime(getContext(), notificationTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));

        mDeferFor30Min = (LinearLayout) view.findViewById(R.id.notification_defer_for_30_min);
        mDeferFor30Min.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotificationToDefer == null) {
                    dismiss();
                    return;
                }

                mNotificationToDefer.getDate().add(Calendar.MINUTE, 30);

                long status = mDbHelper.updateNotification(mNotificationToDefer);

                if(status > 0){
                    updateNotification();
                }

                if(mEventListener != null)
                    mEventListener.onNotificationDefered();

                dismiss();
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

                mNotificationToDefer.getDate().add(Calendar.HOUR, 1);

                long status = mDbHelper.updateNotification(mNotificationToDefer);

                if(status > 0){
                    updateNotification();
                }

                if(mEventListener != null)
                    mEventListener.onNotificationDefered();

                dismiss();
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

                mNotificationToDefer.getDate().add(Calendar.HOUR, 3);

                long status = mDbHelper.updateNotification(mNotificationToDefer);

                if(status > 0){
                    updateNotification();
                }

                if(mEventListener != null)
                    mEventListener.onNotificationDefered();

                dismiss();
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

                mNotificationToDefer.getDate().add(Calendar.HOUR, 6);

                long status = mDbHelper.updateNotification(mNotificationToDefer);

                if(status > 0){
                    updateNotification();
                }

                if(mEventListener != null)
                    mEventListener.onNotificationDefered();

                dismiss();
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

    @Override
    public void onDismiss(DialogInterface dialog) {
        if(mEventListener != null)
            mEventListener.onDismissed();

        super.onDismiss(dialog);
    }

    TimePickerDialog.OnTimeSetListener s = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mNotificationToDefer.getDate().set(Calendar.HOUR_OF_DAY, hourOfDay);
            mNotificationToDefer.getDate().set(Calendar.MINUTE, minute);

            long status = mDbHelper.updateNotification(mNotificationToDefer);

            if(status > 0){
                updateNotification();
            }

            if(mEventListener != null)
                mEventListener.onNotificationDefered();

            dismiss();
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
            alarmIntent.putExtra("type", Notification.getNotificationTypeName(mNotificationToDefer.getType()));

            Long previousIdLong = mNotificationToDefer.getId();
            int previousId = previousIdLong.intValue();
            alarmIntent.putExtra("id", previousId);

            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getContext(), previousId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(previousPendingIntent);

            alarmIntent.getExtras().clear();
        }

        alarmIntent.putExtra("message", mNotificationToDefer.getMessage());
        alarmIntent.putExtra("type", Notification.getNotificationTypeName(mNotificationToDefer.getType()));

        Long idLong = mNotificationToDefer.getId();
        int id = idLong.intValue();
        alarmIntent.putExtra("id", id);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Calendar today = Calendar.getInstance();

        switch (mNotificationToDefer.getType()){
            case Notification.TYPE_ONE_TIME_ID:
                if(today.before(mNotificationToDefer.getDate()))
                    manager.setExact(AlarmManager.RTC_WAKEUP, mNotificationToDefer.getDate().getTimeInMillis(), pendingIntent);
                break;
            case Notification.TYPE_EVERY_DAY_ID:

                Calendar notificationDate = mNotificationToDefer.getDate();
                notificationDate.set(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));

                if(today.after(notificationDate))
                    notificationDate.add(Calendar.DAY_OF_MONTH, 1);

                manager.setRepeating(AlarmManager.RTC_WAKEUP, notificationDate.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
        }
    }

    public void setNotification(Notification notification){
        mNotificationToDefer = notification;
    }

    public void setEventListener(EventListener eventListener){
        mEventListener = eventListener;
    }
}
