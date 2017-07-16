package com.abstractplanner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddTaskFragment extends Fragment {

    private Spinner mSpinnerSelectArea;
    private TextView mSpinnerError;
    private TextInputLayout mTaskNameLayout;
    private TextInputEditText mTaskNameEditText;
    private TextInputLayout mTaskDescriptionLayout;
    private TextInputEditText mTaskDescriptionEditText;
    private TextInputLayout mTaskDateLayout;
    private TextInputEditText mTaskDateEditText;
    private CheckBox mTaskDoneCheckBox;
    private Button mAddTaskButton;

    private String predefinedDay;
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
        mTaskDateLayout = (TextInputLayout) view.findViewById(R.id.et_task_date_layout);
        mTaskDateEditText = (TextInputEditText) view.findViewById(R.id.et_task_date);
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

        if(predefinedDay != null && predefinedAreaName != null){
            mTaskDateEditText.setText(predefinedDay);
            mSpinnerSelectArea.setSelection(spinnerArray.indexOf(predefinedAreaName));
        }

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

                if(mTaskDateEditText.getText().length() <= 0){
                    mTaskDateLayout.setErrorEnabled(true);
                    mTaskDateLayout.setError("You need to enter a date");
                    error = true;
                } else{
                    mTaskDateLayout.setErrorEnabled(false);
                }

                if(error)
                    return;

                Task task = new Task(selectedArea, mTaskNameEditText.getText().toString(),
                        mTaskDescriptionEditText.getText().toString());
                task.setDone(mTaskDoneCheckBox.isChecked());

                List<Day> days = ((MainActivity) getActivity()).days;

                for(int i = 0; i < days.size(); i++){
                    if(days.get(i).getDate().equals(mTaskDateEditText.getText().toString())){
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

    public void setPredefinedParameters(String areaName, String day){
        predefinedAreaName = areaName;
        predefinedDay = day;
    }

}
