package com.abstractplanner.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.abstractplanner.MainActivity;

public class ArchivedAreasDataProviderFragment extends Fragment {
    private ArchivedAreasDataProvider mDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new ArchivedAreasDataProvider(((MainActivity)getActivity()).getDbHelper(), getContext());
    }

    public ArchivedAreasDataProvider getDataProvider() {
        return mDataProvider;
    }
}
