package com.abstractplanner.fragments;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.AreasAdapter;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class EditAreaDialogFragment extends DialogFragment {

    private Toolbar mToolbar;
    private TextInputLayout mAreaNameLayout;
    private TextInputEditText mAreaNameEditText;
    private TextInputLayout mAreaDescriptionLayout;
    private TextInputEditText mAreaDescriptionEditText;
    private Button mAddAreaButton;

    private Area mArea;
    private AreasAdapter mAdapter;
    private AbstractPlannerDatabaseHelper mDbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dialog_edit_area, container, false);

        mToolbar = (Toolbar) view.findViewById(R.id.fragment_dialog_edit_area_toolbar);
        mAreaNameLayout = (TextInputLayout) view.findViewById(R.id.et_area_name_layout);
        mAreaNameEditText = (TextInputEditText) view.findViewById(R.id.et_area_name);
        mAreaDescriptionLayout = (TextInputLayout) view.findViewById(R.id.et_area_description_layout);
        mAreaDescriptionEditText = (TextInputEditText) view.findViewById(R.id.et_area_description);
        mAddAreaButton = (Button) view.findViewById(R.id.button_add_area);
        mAddAreaButton.setVisibility(View.GONE);

        mDbHelper = ((MainActivity)getActivity()).getDbHelper();

        mAreaNameEditText.setText(mArea.getName());
        mAreaDescriptionEditText.setText(mArea.getDescription());

        mToolbar.setTitle("Edit task");
        mToolbar.setNavigationIcon(android.R.drawable.ic_menu_close_clear_cancel);
        //mToolbar.setPadding(0, getStatusBarHeight(), 0, 0);
        setHasOptionsMenu(true);
        ((MainActivity) getActivity()).setSupportActionBar(mToolbar);

        return view;
    }

/*    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }*/

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
                saveEditedArea();
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

    private void saveEditedArea(){
        boolean error = false;

        if(mAreaNameEditText.getText().length() <= 0){
            mAreaNameLayout.setErrorEnabled(true);
            mAreaNameLayout.setError("You need to enter a name");
            error = true;
        }

        if(mAreaDescriptionEditText.getText().length() <= 0){
            mAreaDescriptionLayout.setErrorEnabled(true);
            mAreaDescriptionLayout.setError("You need to enter a description");
            error = true;
        }

        if(error)
            return;

        Area area = new Area(mArea.getId(), mAreaNameEditText.getText().toString(),
                mAreaDescriptionEditText.getText().toString());

        long id = mDbHelper.updateArea(area);

        if(id < 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Name " + area.getName() + " is already in use.")
                    .setTitle("Try another name")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

            builder.show();
        } else {
            mAdapter.saveEditedArea(mArea, area);
            dismiss();
        }
    }

    @Override
    public void onStop() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        super.onStop();
    }

    public void setPrevoisArea(Area previousArea){
        mArea = previousArea;
    }

    public void setAreasAdapter(AreasAdapter areasAdapter){
        mAdapter = areasAdapter;
    }

}
