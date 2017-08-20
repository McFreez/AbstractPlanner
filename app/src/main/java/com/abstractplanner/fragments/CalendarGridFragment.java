package com.abstractplanner.fragments;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.AreasAdapter;
import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.adapters.DaysAdapter;
import com.abstractplanner.data.AbstractPlannerContract;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.table.AreasScrollView;
import com.abstractplanner.table.CenterLayoutManager;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.DataVerticalScrollView;
import com.abstractplanner.table.DaysRecyclerView;
import com.abstractplanner.table.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class CalendarGridFragment extends Fragment
    implements SharedPreferences.OnSharedPreferenceChangeListener{

    private static final String LOG_TAG = "CalendarGridFragment";

    public static final int STARTING_DAYS_COUNT = 60;
    public static final int UPLOAD_DAYS_ON_SCROLL_COUNT = 30;
    public static final int TODAY_INITIAL_POSITION = 29;
    public List<Day> mDays = new ArrayList<>();

    private DaysAdapter daysAdapter;
    private DataAdapter dataAdapter;

    private Toolbar mShortToolbar;

    private AbstractPlannerDatabaseHelper dbHelper;

    private boolean isAreasSortingChanged = false;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_calendar_grid, container, false);

        mShortToolbar = (Toolbar) view.findViewById(R.id.toolbar_short);

/*        gridUpLayout = (LinearLayout) view.findViewById(R.id.grid_up_layout) ;
        gridUpLayout.setVisibility(View.GONE);
        areasAndDataContainer = (LinearLayout) view.findViewById(R.id.areas_and_data_container);
        areasAndDataContainer.setVisibility(View.GONE);
        progressBarContainer = (LinearLayout) view.findViewById(R.id.progress_bar_container);
        progressBarContainer.setVisibility(View.VISIBLE);*/

        CenterLayoutManager layoutManager_data = new CenterLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        final CenterLayoutManager layoutManager_days = new CenterLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        LinearLayout areasContainer = (LinearLayout) view.findViewById(R.id.areas_container);

        DataRecyclerView dataRecyclerView = (DataRecyclerView) view.findViewById(R.id.rv_data);
        dataRecyclerView.setLayoutManager(layoutManager_data);
        dataRecyclerView.setHasFixedSize(true);

        DaysRecyclerView daysRecyclerView = (DaysRecyclerView) view.findViewById(R.id.rv_days);
        daysRecyclerView.setLayoutManager(layoutManager_days);
        daysRecyclerView.setHasFixedSize(true);

        DataVerticalScrollView dataVerticalScrollView = (DataVerticalScrollView) view.findViewById(R.id.data_vertical_scroll);

        AreasScrollView areasScrollView = (AreasScrollView) view.findViewById(R.id.areas_scroll);

        areasScrollView.synchronizeScrollWith(dataVerticalScrollView);
        dataVerticalScrollView.synchronizeScrollingWith(areasScrollView);

        dataRecyclerView.synchronizeScrollingWith(daysRecyclerView);
        daysRecyclerView.synchronizeScrollingWith(dataRecyclerView);

        dbHelper = ((MainActivity) getActivity()).getDbHelper();

        List<Area> areas = new ArrayList<>();

        Cursor areasCursor = dbHelper.getAllAreas();
        for(int i = 0; i < areasCursor.getCount(); i++){
            areasCursor.moveToPosition(i);
            areas.add(new Area(areasCursor.getLong(areasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry._ID)),
                    areasCursor.getString(areasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_NAME)),
                    areasCursor.getString(areasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_DESCRIPTION))));
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String value = sharedPreferences.getString(getString(R.string.pref_areas_sort_key), "");
        if(value.equals(getString(R.string.pref_areas_sort_by_tasks))) {
            Collections.sort(areas, new Comparator<Area>() {
                @Override
                public int compare(Area a, Area b) {

                    int aCount = dbHelper.getUndoneTasksInAreaCount(a.getId());
                    int bCount = dbHelper.getUndoneTasksInAreaCount(b.getId());

                    return aCount > bCount ? -1 : (aCount < bCount) ? 1 : 0;
                }
            });
        }

        daysAdapter = new DaysAdapter(mDays);
        daysRecyclerView.setAdapter(daysAdapter);

        dataAdapter = new DataAdapter(mDays, areas, (MainActivity) getActivity());
        dataRecyclerView.setAdapter(dataAdapter);

        if(mDays.size() == 0) {
            dataAdapter.loadInitialDaysData(daysAdapter);
        }

        /*AreasAdapter areasAdapter = */new AreasAdapter((MainActivity) getActivity(), areas, areasContainer);

        EndlessRecyclerViewScrollListener dataRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager_data) {
            @Override
            public void onScrollForwardLoadMore(final EndlessRecyclerViewScrollListener scrollListener) {

                dataAdapter.loadNextDaysData(daysAdapter, scrollListener, UPLOAD_DAYS_ON_SCROLL_COUNT);

            }

            @Override
            public void onScrollBackwardLoadMore(final EndlessRecyclerViewScrollListener scrollListener) {

                dataAdapter.loadPreviousDaysData(daysAdapter, scrollListener, UPLOAD_DAYS_ON_SCROLL_COUNT);

            }
        };
        dataRecyclerView.addOnScrollListener(dataRecyclerViewScrollListener);
        //daysRecyclerView.scrollToToday();
        dataRecyclerView.scrollToToday();

        ImageView buttonAddArea = (ImageView) view.findViewById(R.id.button_add_area);
        buttonAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).displaySelectedScreen(R.id.add_area, null);
            }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(isAreasSortingChanged)
            ((MainActivity)getActivity()).displaySelectedScreen(R.id.calendar_grid, null);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        /* Unregister the preference change listener */
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        /* Register the preference change listener */
        PreferenceManager.getDefaultSharedPreferences(getContext())
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).setShortAppBar(mShortToolbar);

        //dataRecyclerView.smoothScrollToToday();
    }

    @Override
    public void onDestroyView() {
        ((MainActivity)getActivity()).setLongAppBar(mShortToolbar);

        super.onDestroyView();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(key.equals(getString(R.string.pref_areas_sort_key))){
            isAreasSortingChanged = true;
        }
    }
}