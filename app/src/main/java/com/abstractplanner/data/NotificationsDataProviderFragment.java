package com.abstractplanner.data;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.abstractplanner.MainActivity;

public class NotificationsDataProviderFragment extends Fragment {
    private AbstractDataProvider mDataProvider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);  // keep the mDataProvider instance
        mDataProvider = new NotificationsDataProvider(((MainActivity)getActivity()).getDbHelper(), getContext());
    }

    public AbstractDataProvider getDataProvider() {
        return mDataProvider;
    }
}
