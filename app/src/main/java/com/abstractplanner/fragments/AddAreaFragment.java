package com.abstractplanner.fragments;


import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddAreaFragment extends Fragment {

    private TextInputLayout mAreaNameLayout;
    private TextInputEditText mAreaNameEditText;
    private TextInputLayout mAreaDescriptionLayout;
    private TextInputEditText mAreaDescriptionEditText;
    private Button mAddAreaButton;

    private AbstractPlannerDatabaseHelper mDbHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_area, container, false);

        mAreaNameLayout = (TextInputLayout) view.findViewById(R.id.et_area_name_layout);
        mAreaNameEditText = (TextInputEditText) view.findViewById(R.id.et_area_name);
        mAreaDescriptionLayout = (TextInputLayout) view.findViewById(R.id.et_area_description_layout);
        mAreaDescriptionEditText = (TextInputEditText) view.findViewById(R.id.et_area_description);
        mAddAreaButton = (Button) view.findViewById(R.id.button_add_area);
        mDbHelper = ((MainActivity)getActivity()).getDbHelper();
        mAddAreaButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                boolean error = false;

                if(mAreaNameEditText.getText().length() <= 0){
                    mAreaNameLayout.setErrorEnabled(true);
                    mAreaNameLayout.setError("You need to enter a name");
                    error = true;
                }

/*                if(mAreaDescriptionEditText.getText().length() <= 0){
                    mAreaDescriptionLayout.setErrorEnabled(true);
                    mAreaDescriptionLayout.setError("You need to enter a description");
                    error = true;
                }*/

                if(error)
                    return;

                Area area = new Area(mAreaNameEditText.getText().toString(),
                        mAreaDescriptionEditText.getText().toString());

                long id = mDbHelper.createArea(area);
                //((MainActivity) getActivity()).areas.add(area);

                if(id < 0) {
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
                } else
                    ((MainActivity) getActivity()).displaySelectedScreen(R.id.calendar_grid, null);

            }
        });

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getActivity().setTitle("New area");
    }

    @Override
    public void onStop() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

        super.onStop();
    }

}
