package com.abstractplanner.fragments;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.TodayTasksAdapter;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Task;

public class AddQuickTaskDialogFragment extends DialogFragment {

    private Toolbar mToolbar;

    private EditText mTaskNameEditText;
    private EditText mTaskDescriptionEditText;
    private TodayTasksAdapter mAdapter;
    private Task mTaskToEdit;

    private AbstractPlannerDatabaseHelper mDbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_add_quick_task, container, false);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        mToolbar = (Toolbar) view.findViewById(R.id.fragment_dialog_add_quick_task_toolbar);

        mTaskNameEditText = (EditText) view.findViewById(R.id.et_quick_task_name);
        mTaskDescriptionEditText = (EditText) view.findViewById(R.id.et_quick_task_description);

        if(mTaskToEdit != null){
            mTaskNameEditText.setText(mTaskToEdit.getName());
            mTaskDescriptionEditText.setText(mTaskToEdit.getDescription());
            mToolbar.setTitle("Edit quick task");
        } else
            mToolbar.setTitle("Create quick task");

        mToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        //mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);

        return view;
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
        getActivity().getMenuInflater().inflate(R.menu.add_item_dialog_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_save) {
            // handle confirmation button click here
            if(mAdapter != null){
                InputMethodManager inputMethodManager = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
                saveTask();
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

    private void saveTask(){
        if(mTaskNameEditText.getText().toString().equals("") && mTaskDescriptionEditText.getText().toString().equals("")){
            return;
        }

        Task task;

        if(mTaskToEdit != null){
            task = new Task(mTaskToEdit.getId(),
                    null,
                    mTaskNameEditText.getText().toString(),
                    mTaskDescriptionEditText.getText().toString(),
                    null,
                    false,
                    Task.TYPE_QUICK);

            long status = mDbHelper.updateTask(task);

            if(status < 0){
                Toast.makeText(getContext(), "Failed to update task", Toast.LENGTH_SHORT).show();
            } else {
                mAdapter.saveQuickTask(task);
                dismiss();
            }
        } else {
            task = new Task(null,
                    mTaskNameEditText.getText().toString(),
                    mTaskDescriptionEditText.getText().toString(),
                    null,
                    false,
                    Task.TYPE_QUICK);

            long id = mDbHelper.createTask(task);
            task.setId(id);

            if(id <= 0){
                Toast.makeText(getContext(), "Failed to create task", Toast.LENGTH_SHORT).show();
            } else {
                mAdapter.addQuickTask(task);
                dismiss();
            }
        }
    }

    public void setAdapter(TodayTasksAdapter adapter){
        mAdapter = adapter;
    }

    public void setTaskToEdit(Task task){
        mTaskToEdit = task;
    }
}
