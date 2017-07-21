package com.abstractplanner.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;


public class TasksDataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new TasksDataProvider();
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}
