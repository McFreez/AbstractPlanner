package com.abstractplanner.fragments;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.NotificationsAdapter;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.receivers.AlarmReceiver;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class EditNotificationDialogFragment extends DialogFragment {

    private Toolbar mToolbar;
    private Spinner mNotificationTypeSpinner;
    private TextInputLayout mMessageLayout;
    private TextInputEditText mMessageEditText;
    private TextInputLayout mDateLayout;
    private TextInputEditText mDateEditText;
    private TextInputLayout mTimeLayout;
    private TextInputEditText mTimeEditText;
    private TextInputLayout mNotificationTaskLayout;
    private TextInputEditText mNotificationTaskEditText;

    private Calendar mNotificationDateTime;
    private Notification mNotificationToEdit;
    //private Task mNotificationTask;

    private AbstractPlannerDatabaseHelper mDbHelper;
    private NotificationsAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_dialog_edit_notification, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.fragment_dialog_edit_notification_toolbar);

        mNotificationTypeSpinner = (Spinner) view.findViewById(R.id.spinner_select_notification_type);
        mMessageLayout = (TextInputLayout) view.findViewById(R.id.et_notification_message_layout);
        mMessageEditText = (TextInputEditText) view.findViewById(R.id.et_notification_message);
        mDateLayout = (TextInputLayout) view.findViewById(R.id.et_notification_date_layout);
        mDateEditText = (TextInputEditText) view.findViewById(R.id.et_notification_date);
        mDateEditText.setKeyListener(null);
        mTimeLayout = (TextInputLayout) view.findViewById(R.id.et_notification_time_layout);
        mTimeEditText = (TextInputEditText) view.findViewById(R.id.et_notification_time);
        mTimeEditText.setKeyListener(null);
        mNotificationTaskLayout = (TextInputLayout) view.findViewById(R.id.et_notification_task_layout);
        mNotificationTaskEditText = (TextInputEditText) view.findViewById(R.id.et_notification_task);
        mNotificationTaskLayout.setEnabled(false);
        mNotificationTaskLayout.setVisibility(View.GONE);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        List<String> spinnerNotificationTypes = new ArrayList<String>();

        spinnerNotificationTypes.add(Notification.TYPE_ONE_TIME_NAME);
        spinnerNotificationTypes.add(Notification.TYPE_EVERY_DAY_NAME);

        ArrayAdapter<String> mSpinnerAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, spinnerNotificationTypes);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mNotificationTypeSpinner.setAdapter(mSpinnerAdapter);

        if(mNotificationDateTime == null)
            mNotificationDateTime = Calendar.getInstance();

/*
        if(mNotificationTask != null)
            if(mNotificationDateTime.before(mNotificationTask.getDate()))
                mNotificationDateTime.set(mNotificationTask.getDate().get(Calendar.YEAR), mNotificationTask.getDate().get(Calendar.MONTH), mNotificationTask.getDate().get(Calendar.DAY_OF_MONTH));
*/

        setDateString();
        setTimeString();

        if(mNotificationToEdit != null) {
            mMessageEditText.setText(mNotificationToEdit.getMessage());
            mNotificationTypeSpinner.setSelection(spinnerNotificationTypes.indexOf(Notification.getNotificationTypeName(mNotificationToEdit.getType())));
            if(mNotificationToEdit.getTask() != null) {
                mNotificationTaskLayout.setVisibility(View.VISIBLE);
                mNotificationTaskEditText.setText(mNotificationToEdit.getTask().getName() + " - " + mNotificationToEdit.getTask().getArea().getName());
                mNotificationTypeSpinner.setSelection(spinnerNotificationTypes.indexOf(Notification.TYPE_ONE_TIME_NAME));
                mNotificationTypeSpinner.setEnabled(false);
            }

            if(mNotificationToEdit.getType() == Notification.TYPE_SYSTEM_ID){
                mNotificationTypeSpinner.setVisibility(View.GONE);
                View splitter = view.findViewById(R.id.spinner_splitter);
                splitter.setVisibility(View.GONE);

                mDateLayout.setVisibility(View.GONE);
                mDateEditText.setVisibility(View.GONE);
                mMessageLayout.setEnabled(false);
/*                mNotificationDateTime.setTimeInMillis(mNotificationToEdit.getDate().getTimeInMillis());
                setDateString();
                setTimeString();*/
            }
        }
/*        else
            if(mNotificationTask != null){
                mNotificationTaskLayout.setVisibility(View.VISIBLE);
                mNotificationTaskEditText.setText(mNotificationTask.getName() + " - " + mNotificationTask.getArea().getName());
                mNotificationTypeSpinner.setSelection(spinnerNotificationTypes.indexOf(Notification.TYPE_ONE_TIME_NAME));
                mNotificationTypeSpinner.setEnabled(false);
            }*/


        View.OnClickListener setDateClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(view);
            }
        };

        mDateLayout.setOnClickListener(setDateClickListener);
        mDateEditText.setOnClickListener(setDateClickListener);
        mDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    setDate(view);
            }
        });

        View.OnClickListener setTimeClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setTime(view);
            }
        };

        mTimeLayout.setOnClickListener(setTimeClickListener);
        mTimeEditText.setOnClickListener(setTimeClickListener);
        mTimeEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(b)
                    setTime(view);
            }
        });

        mNotificationTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedItem = adapterView.getSelectedItem().toString();

                switch (selectedItem){
                    case Notification.TYPE_EVERY_DAY_NAME:
                        mDateLayout.setEnabled(false);

                        mDateLayout.setOnClickListener(null);
                        mDateEditText.setOnClickListener(null);
                        mDateEditText.setOnFocusChangeListener(null);

                        break;
                    case Notification.TYPE_ONE_TIME_NAME:
                        mDateLayout.setEnabled(true);

                        View.OnClickListener setDateClickListener = new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setDate(view);
                            }
                        };

                        mDateLayout.setOnClickListener(setDateClickListener);
                        mDateEditText.setOnClickListener(setDateClickListener);
                        mDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                            @Override
                            public void onFocusChange(View view, boolean b) {
                                if(b)
                                    setDate(view);
                            }
                        });

                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        if(mNotificationToEdit == null)
            mToolbar.setTitle("New notification");
        else
            mToolbar.setTitle("Edit notification");
        mToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        //mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);

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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        getActivity().getMenuInflater().inflate(R.menu.add_item_dialog_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // handle confirmation button click here
            //if(mAdapter != null){
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                saveNotification();
            //}
            return true;
        } else if (id == android.R.id.home) {
            // handle close button click here
            InputMethodManager inputMethodManager = (InputMethodManager)
                    getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            dismiss();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void saveNotification(){

        boolean error = false;

        String selectedNotificationType = mNotificationTypeSpinner.getSelectedItem().toString();

        if(mMessageEditText.getText().length() <= 0){
            mMessageLayout.setErrorEnabled(true);
            mMessageLayout.setError("You need to enter a name");
            error = true;
        }
        else{
            mMessageLayout.setErrorEnabled(false);
        }

        if(error)
            return;

        Task task = null;
        if(mNotificationToEdit != null)
            task = mNotificationToEdit.getTask();

        Notification notification = new Notification(mMessageEditText.getText().toString(),
                mNotificationDateTime,
                task,
                Notification.getNotificationTypeID(selectedNotificationType));

        if(mNotificationToEdit != null)
            if(mNotificationToEdit.getType() == Notification.TYPE_SYSTEM_ID)
                notification.setType(Notification.TYPE_SYSTEM_ID);

        long id;
        boolean updated = false;
        if(mNotificationToEdit == null)
            id = mDbHelper.createNotification(notification);
        else {
            updated = true;
            notification.setId(mNotificationToEdit.getId());
            id = mDbHelper.updateNotification(notification);
        }

        if(id < 0){
            Toast.makeText(getContext(), "Cannot create notification", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
            if(!updated)
                notification.setId(id);

            createOrUpdateNotification(notification);
            if(mAdapter != null)
                mAdapter.saveNotification(notification);
            dismiss();
        }
    }

    private void createOrUpdateNotification(Notification notification){
        AlarmManager manager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(getActivity(), AlarmReceiver.class);

        if(mNotificationToEdit != null) {
            //Intent previousAlarmIntent = new Intent(getActivity(), AlarmReceiver.class);
            alarmIntent.putExtra("message", mNotificationToEdit.getMessage());
            if(mNotificationToEdit.getTask() != null) {
                if(mNotificationToEdit.getTask().getType() == Task.TYPE_QUICK){
                    alarmIntent.putExtra("title", getString(R.string.quick_task_title));
                } else
                    alarmIntent.putExtra("title", mNotificationToEdit.getTask().getArea().getName());
            }
            else
                alarmIntent.putExtra("title", Notification.getNotificationTypeName(mNotificationToEdit.getType()) + " notification");

            alarmIntent.putExtra("id", mNotificationToEdit.getId());

            Long previousIdLong = mNotificationToEdit.getId();
            int previousId = previousIdLong.intValue();

            PendingIntent previousPendingIntent = PendingIntent.getBroadcast(getContext(), previousId, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            manager.cancel(previousPendingIntent);

            alarmIntent.getExtras().clear();
        }

        alarmIntent.putExtra("message", notification.getMessage());
        if(notification.getTask() != null) {
            if(notification.getTask().getType() == Task.TYPE_QUICK){
                alarmIntent.putExtra("title", getString(R.string.quick_task_title));
            } else
                alarmIntent.putExtra("title", notification.getTask().getArea().getName());
        }
        else
            alarmIntent.putExtra("title", Notification.getNotificationTypeName(notification.getType()) + " notification");

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

    @Override
    public void onStop() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        super.onStop();
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mNotificationDateTime.set(Calendar.YEAR, year);
            mNotificationDateTime.set(Calendar.MONTH, monthOfYear);
            mNotificationDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDateString();
        }
    };

    // отображаем диалоговое окно для выбора даты
    private void setDate(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        new DatePickerDialog(getContext(), d,
                mNotificationDateTime.get(Calendar.YEAR),
                mNotificationDateTime.get(Calendar.MONTH),
                mNotificationDateTime.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setDateString(){
        mDateEditText.setText(DateUtils.formatDateTime(getContext(), mNotificationDateTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    TimePickerDialog.OnTimeSetListener s = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
            mNotificationDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
            mNotificationDateTime.set(Calendar.MINUTE, minute);
            setTimeString();
        }
    };

    private void setTime(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        new TimePickerDialog(getContext(), s,
                mNotificationDateTime.get(Calendar.HOUR_OF_DAY),
                mNotificationDateTime.get(Calendar.MINUTE),
                true)
                .show();
    }

    private void setTimeString(){
        mTimeEditText.setText(DateUtils.formatDateTime(getContext(), mNotificationDateTime.getTimeInMillis(), DateUtils.FORMAT_SHOW_TIME | DateUtils.FORMAT_24HOUR));
    }

    public void setAdapter(NotificationsAdapter adapter){
        mAdapter = adapter;
    }

    public void setNotificationToEdit(Notification notificationToEdit){
        mNotificationToEdit = notificationToEdit;

        if(mNotificationToEdit.getType() == Notification.TYPE_EVERY_DAY_ID) {
            mNotificationDateTime = Calendar.getInstance();
            mNotificationDateTime.set(Calendar.HOUR_OF_DAY, notificationToEdit.getDate().get(Calendar.HOUR_OF_DAY));
            mNotificationDateTime.set(Calendar.MINUTE, notificationToEdit.getDate().get(Calendar.MINUTE));
        }else
            if(mNotificationToEdit.getType() == Notification.TYPE_ONE_TIME_ID){
                mNotificationDateTime = new GregorianCalendar(notificationToEdit.getDate().get(Calendar.YEAR),
                        notificationToEdit.getDate().get(Calendar.MONTH),
                        notificationToEdit.getDate().get(Calendar.DAY_OF_MONTH),
                        notificationToEdit.getDate().get(Calendar.HOUR_OF_DAY),
                        notificationToEdit.getDate().get(Calendar.MINUTE));
            }
    }

/*    public void setNotificationTask(Task task){
        mNotificationTask = task;
    }*/
}