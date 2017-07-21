package com.abstractplanner.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;

import java.util.ArrayList;
import java.util.Calendar;
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
    private LinearLayout mTaskDateLayout;
    private TextView mTaskDateTextView;
    private Calendar mTaskDate;
    private CheckBox mTaskDoneCheckBox;
    private Button mAddTaskButton;

    private String predefinedAreaName;

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
        mTaskDateLayout = (LinearLayout) view.findViewById(R.id.task_date_layout);
        mTaskDateTextView = (TextView) view.findViewById(R.id.task_date);
        mTaskDoneCheckBox = (CheckBox) view.findViewById(R.id.checkBox_task_done);
        mAddTaskButton = (Button) view.findViewById(R.id.button_add_task);

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

        if(predefinedAreaName != null){
            mSpinnerSelectArea.setSelection(spinnerArray.indexOf(predefinedAreaName));
        }

        setDateString();

        if(areas.size() == 0) {
            mSpinnerError.setVisibility(View.VISIBLE);
        }

        mTaskDateLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDate(view);
            }
        });

        mAddTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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

/*                if(mTaskDateEditText.getText().length() <= 0){
                    mTaskDateLayout.setErrorEnabled(true);
                    mTaskDateLayout.setError("You need to enter a date");
                    error = true;
                } else{
                    mTaskDateLayout.setErrorEnabled(false);
                }*/

                if(error)
                    return;

                Task task = new Task(selectedArea, mTaskNameEditText.getText().toString(),
                        mTaskDescriptionEditText.getText().toString(), 0);
                task.setDone(mTaskDoneCheckBox.isChecked());

                List<Day> days = ((MainActivity) getActivity()).days;

                mTaskDate.set(Calendar.HOUR_OF_DAY, 0);
                mTaskDate.set(Calendar.MINUTE, 0);
                mTaskDate.set(Calendar.SECOND, 0);
                mTaskDate.set(Calendar.MILLISECOND, 0);

                for(int i = 0; i < days.size(); i++){
                    if(days.get(i).getDate().compareTo(mTaskDate) == 0){
                        days.get(i).addTask(task);
                    }
                }

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
        new DatePickerDialog(getContext(), d,
                mTaskDate.get(Calendar.YEAR),
                mTaskDate.get(Calendar.MONTH),
                mTaskDate.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    private void setDateString(){
        mTaskDateTextView.setText(DateUtils.formatDateTime(getContext(), mTaskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
    }

    public void setPredefinedParameters(String areaName, Calendar date){
        predefinedAreaName = areaName;
        mTaskDate = date;
    }

}
