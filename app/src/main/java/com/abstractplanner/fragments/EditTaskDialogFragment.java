package com.abstractplanner.fragments;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class EditTaskDialogFragment extends DialogFragment {

    private Task mTask;

    private Toolbar mToolbar;
    private Spinner mSpinnerSelectArea;
    private TextView mSpinnerError;
    private TextInputLayout mTaskNameLayout;
    private TextInputEditText mTaskNameEditText;
    private TextInputLayout mTaskDescriptionLayout;
    private TextInputEditText mTaskDescriptionEditText;
    private LinearLayout mTaskDateLayout;
    private TextView mTaskDateTextView;
    private Calendar mTaskPreviousDate;
    private Calendar mTaskDate;
    private CheckBox mTaskDoneCheckBox;
    private Button mSaveTaskButton;
    private DataAdapter mAdapter;

/*    public EditTaskDialogFragment(Context context){
        super(context, android.R.style.F);
    }*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout to use as dialog or embedded fragment
        View view = inflater.inflate(R.layout.fragment_dialog_add_task, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.fragment_dialog_add_task_toolbar);
        mSpinnerSelectArea = (Spinner) view.findViewById(R.id.spinner_select_area);
        mSpinnerError = (TextView) view.findViewById(R.id.tv_areas_error);
        mTaskNameLayout = (TextInputLayout) view.findViewById(R.id.et_task_name_layout);
        mTaskNameEditText = (TextInputEditText) view.findViewById(R.id.et_task_name);
        mTaskDescriptionLayout = (TextInputLayout) view.findViewById(R.id.et_task_description_layout);
        mTaskDescriptionEditText = (TextInputEditText) view.findViewById(R.id.et_task_description);
        mTaskDateLayout = (LinearLayout) view.findViewById(R.id.task_date_layout);
        mTaskDateTextView = (TextView) view.findViewById(R.id.task_date);
        mTaskDoneCheckBox = (CheckBox) view.findViewById(R.id.checkBox_task_done);
        mSaveTaskButton = (Button) view.findViewById(R.id.button_add_task);
        mSaveTaskButton.setVisibility(View.GONE);

        List<String> spinnerArray =  new ArrayList<String>();
        List<Area> areas = ((MainActivity)getActivity()).areas;

        for(int i = 0; i < areas.size(); i++){
            spinnerArray.add(areas.get(i).getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, spinnerArray);

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSelectArea.setAdapter(adapter);

        if(mTaskDate == null){
            mTaskDate = Calendar.getInstance();
        }

        setDateString();

        if(areas.size() == 0) {
            mSpinnerError.setVisibility(View.VISIBLE);
        }

        mSpinnerSelectArea.setSelection(spinnerArray.indexOf(mTask.getArea().getName()));
        mTaskNameEditText.setText(mTask.getName());
        mTaskDescriptionEditText.setText(mTask.getDescription());
        mTaskDoneCheckBox.setChecked(mTask.isDone());

        mTaskDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(view);
            }
        });

        mToolbar.setTitle("Edit task");
        mToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);

        return view;
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /** The system calls this only when creating the layout in a dialog. */
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
        getActivity().getMenuInflater().inflate(R.menu.add_task_dialog_menu, menu);
    }

    DatePickerDialog.OnDateSetListener d = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            mTaskDate.set(Calendar.YEAR, year);
            mTaskDate.set(Calendar.MONTH, monthOfYear);
            mTaskDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDateString();
        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // handle confirmation button click here
            if(mAdapter != null){
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                saveNewTask();
            }
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

    private void saveNewTask(){

        boolean error = false;

        List<Area> areas = ((MainActivity)getActivity()).areas;

        if(areas.size() == 0) {
            mSpinnerError.setVisibility(View.VISIBLE);
            return;
        }

        Area selectedArea = null;
        String selectedAreaName = mSpinnerSelectArea.getSelectedItem().toString();
        for (Area a : areas) {
            if (a.getName().equals(selectedAreaName))
                selectedArea = a;
        }

        if(selectedArea == null)
            error = true;

        if(mTaskNameEditText.getText().length() <= 0){
            mTaskNameLayout.setErrorEnabled(true);
            mTaskNameLayout.setError("You need to enter a name");
            error = true;
        }
        else{
            mTaskNameLayout.setErrorEnabled(false);
        }

        if(mTaskDescriptionEditText.getText().length() <= 0){
            mTaskDescriptionLayout.setErrorEnabled(true);
            mTaskDescriptionLayout.setError("You need to enter a description");
            error = true;
        } else{
            mTaskDescriptionLayout.setErrorEnabled(false);
        }

        if(error)
            return;

        Task task = new Task(selectedArea, mTaskNameEditText.getText().toString(),
                mTaskDescriptionEditText.getText().toString());
        task.setDone(mTaskDoneCheckBox.isChecked());

        mTaskDate.set(Calendar.HOUR_OF_DAY, 0);
        mTaskDate.set(Calendar.MINUTE, 0);
        mTaskDate.set(Calendar.SECOND, 0);
        mTaskDate.set(Calendar.MILLISECOND, 0);

        mAdapter.saveEditedTask(mTask, mTaskPreviousDate, task, mTaskDate);

        dismiss();
    }

    // отображаем диалоговое окно для выбора даты
    private void setDate(View v) {
        new DatePickerDialog(getContext(), d,
                mTaskDate.get(Calendar.YEAR),
                mTaskDate.get(Calendar.MONTH),
                mTaskDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setDateString(){
        mTaskDateTextView.setText(DateUtils.formatDateTime(getContext(), mTaskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    public void setTask(Task task){
        mTask = task;
    }

    public void setTaskDate(Calendar date){
        mTaskPreviousDate = date;

        mTaskDate = new GregorianCalendar(
                date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
        mTaskDate.set(Calendar.HOUR_OF_DAY, 0);
        mTaskDate.set(Calendar.MINUTE, 0);
        mTaskDate.set(Calendar.SECOND, 0);
        mTaskDate.set(Calendar.MILLISECOND, 0);
    }

    public void setDataAdapter(DataAdapter adapter){
        mAdapter = adapter;
    }
}
