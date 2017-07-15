package com.abstractplanner.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.dto.Attribute;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class AddAttributeFragment extends Fragment {

    private EditText mAttributeNameEditText;
    private EditText mAttributeDescriptionEditText;
    private Button mAddAttributeButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_add_attribute, container, false);

        mAttributeNameEditText = (EditText) view.findViewById(R.id.et_attribute_name);
        mAttributeDescriptionEditText = (EditText) view.findViewById(R.id.et_attribute_description);
        mAddAttributeButton = (Button) view.findViewById(R.id.button_add_attribute);
        mAddAttributeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAttributeNameEditText.getText().length() > 0 && mAttributeDescriptionEditText.getText().length() > 0){
                    Attribute attribute = new Attribute(mAttributeNameEditText.getText().toString(),
                            mAttributeDescriptionEditText.getText().toString());

                    ((MainActivity) getActivity()).attributes.add(attribute);

                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                            INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);

                    ((MainActivity) getActivity()).displaySelectedScreen(R.id.calendar_grid);
                }
            }
        });


        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }
}
