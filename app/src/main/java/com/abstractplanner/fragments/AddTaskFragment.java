package com.abstractplanner.fragments;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerContract;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Task;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddTaskFragment extends Fragment {

    private static final String LOG_TAG = "AddTaskFragment";

    private Spinner mSpinnerSelectArea;
    private TextView mSpinnerError;
    private TextInputLayout mTaskNameLayout;
    private TextInputEditText mTaskNameEditText;
    private TextInputLayout mTaskDescriptionLayout;
    private TextInputEditText mTaskDescriptionEditText;
    private TextInputLayout mTaskDateInputLayout;
    private TextInputEditText mTaskDateEditText;
    private Calendar mTaskDate;
    private CheckBox mTaskDoneCheckBox;
    private Button mAddTaskButton;

    private String predefinedAreaName;
    private AbstractPlannerDatabaseHelper mDbHelper;
    private ArrayAdapter<String> mSpinnerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_task, container, false);

        mSpinnerSelectArea = (Spinner) view.findViewById(R.id.spinner_select_area);
        mSpinnerError = (TextView) view.findViewById(R.id.tv_areas_error);
        mTaskNameLayout = (TextInputLayout) view.findViewById(R.id.et_task_name_layout);
        mTaskNameEditText = (TextInputEditText) view.findViewById(R.id.et_task_name);
        mTaskDescriptionLayout = (TextInputLayout) view.findViewById(R.id.et_task_description_layout);
        mTaskDescriptionEditText = (TextInputEditText) view.findViewById(R.id.et_task_description);
        mTaskDateInputLayout = (TextInputLayout) view.findViewById(R.id.et_task_date_layout);
        mTaskDateEditText = (TextInputEditText) view.findViewById(R.id.et_task_date);
        mTaskDateEditText.setKeyListener(null);
        mTaskDoneCheckBox = (CheckBox) view.findViewById(R.id.checkBox_task_done);
        mAddTaskButton = (Button) view.findViewById(R.id.button_add_task);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        List<String> spinnerAreas = new ArrayList<String>();

        Cursor areasCursor = mDbHelper.getNotArchivedAreas();
        for(int i = 0; i < areasCursor.getCount(); i++){
            areasCursor.moveToPosition(i);
            spinnerAreas.add(areasCursor.getString(areasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_NAME)));
        }

        mSpinnerAdapter = new ArrayAdapter<String>(
                getContext(), android.R.layout.simple_spinner_item, spinnerAreas);

        mSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerSelectArea.setAdapter(mSpinnerAdapter);

        if(mTaskDate == null){
            mTaskDate = Calendar.getInstance();
        }

        setDateString();

        if(predefinedAreaName != null){
            mSpinnerSelectArea.setSelection(spinnerAreas.indexOf(predefinedAreaName));
            mTaskDateInputLayout.setVisibility(View.GONE);
            mTaskDateInputLayout.setEnabled(false);
            mTaskDateEditText.setEnabled(false);
        } else {
            View.OnClickListener setDateClickListener = new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setDate(view);
                }
            };

            mTaskDateInputLayout.setOnClickListener(setDateClickListener);
            mTaskDateEditText.setOnClickListener(setDateClickListener);
            mTaskDateEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if(b)
                        setDate(view);
                }
            });
        }

        if(spinnerAreas.size() == 0) {
            mSpinnerError.setVisibility(View.VISIBLE);
        }


        mAddTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean error = false;

                if(mSpinnerAdapter.getCount() == 0) {
                    mSpinnerError.setVisibility(View.VISIBLE);
                    return;
                }

                String selectedAreaName = mSpinnerSelectArea.getSelectedItem().toString();
                Area selectedArea = mDbHelper.getAreaByName(selectedAreaName);

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

/*                if(mTaskDescriptionEditText.getText().length() <= 0){
                    mTaskDescriptionLayout.setErrorEnabled(true);
                    mTaskDescriptionLayout.setError("You need to enter a description");
                    error = true;
                } else{
                    mTaskDescriptionLayout.setErrorEnabled(false);
                }*/

                if(error)
                    return;

                mTaskDate.set(Calendar.HOUR_OF_DAY, 0);
                mTaskDate.set(Calendar.MINUTE, 0);
                mTaskDate.set(Calendar.SECOND, 0);
                mTaskDate.set(Calendar.MILLISECOND, 0);

                Task task = new Task(selectedArea, mTaskNameEditText.getText().toString(),
                        mTaskDescriptionEditText.getText().toString(), mTaskDate, mTaskDoneCheckBox.isChecked(), Task.TYPE_NORMAL);

                long id = mDbHelper.createTask(task);

                if(id < 0){
                    if(id == -1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setMessage("You already have task for "
                                + DateUtils.formatDateTime(getContext(), task.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                                + " on " + task.getArea().getName() + ".")
                                .setTitle("Try another day or area")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });

                        builder.show();
                    }
                    else
                        if(id == -2){
                            long taskId = mDbHelper.isAllPreviousAreaTasksDone(task);
                            if (taskId > 0) {
                                Task undoneTask = mDbHelper.getTaskByID(taskId);

                                Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                                Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                                String dateString;

                                if (undoneTask.getDate().after(previousYear) && undoneTask.getDate().before(nextYear))
                                    dateString = DateUtils.formatDateTime(getContext(), undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                                else
                                    dateString = DateUtils.formatDateTime(getContext(), undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                                builder.setMessage("You have unfinished task for " + dateString + " in " + task.getArea().getName() + ". Finish it first or create undone task."
                                        + " To create undone task, uncheck field 'Task is done'.")
                                        .setTitle("Create undone task")
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });

                                builder.show();
                            }
                        }
                } else
                    ((MainActivity) getActivity()).displaySelectedScreen(R.id.calendar_grid, null);

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("New task");
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
            mTaskDate.set(Calendar.YEAR, year);
            mTaskDate.set(Calendar.MONTH, monthOfYear);
            mTaskDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            setDateString();
        }
    };

    // отображаем диалоговое окно для выбора даты
    private void setDate(View v) {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        new DatePickerDialog(getContext(), d,
                mTaskDate.get(Calendar.YEAR),
                mTaskDate.get(Calendar.MONTH),
                mTaskDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setDateString(){
        mTaskDateEditText.setText(DateUtils.formatDateTime(getContext(), mTaskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    public void setPredefinedParameters(String areaName, Calendar date){
        predefinedAreaName = areaName;
        mTaskDate = date;
    }

}
