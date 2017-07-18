package com.abstractplanner.fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.table.AreasScrollView;
import com.abstractplanner.table.CenterLayoutManager;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.DataVerticalScrollView;
import com.abstractplanner.table.DaysRecyclerView;
import com.abstractplanner.table.EndlessRecyclerViewScrollListener;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

public class CalendarGridFragment extends Fragment{

    private static final String LOG_TAG = "CalendarGridFragment";

    private Toolbar mShortToolbar;
    private static int AREAS_COUNT;
    public static final int STARTING_DAYS_COUNT = 60;
    public static final int UPLOAD_DAYS_ON_SCROLL_COUNT = 30;

    private AreasAdapter areasAdapter;
    private DaysAdapter daysAdapter;
    private DataAdapter dataAdapter;

    private AreasScrollView areasScrollView;
    private LinearLayout areasContainer;
    private DataRecyclerView dataRecyclerView;
    private EndlessRecyclerViewScrollListener dataRecyclerViewScrollListener;
    private CenterLayoutManager layoutManager_data;
    private DaysRecyclerView daysRecyclerView;
    private DataVerticalScrollView dataVerticalScrollView;

    private ImageView buttonAddArea;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.fragment_calendar_grid, container, false);

        mShortToolbar = (Toolbar) view.findViewById(R.id.toolbar_short);

        layoutManager_data = new CenterLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        final LinearLayoutManager layoutManager_days = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        areasContainer = (LinearLayout) view.findViewById(R.id.areas_container);

        dataRecyclerView = (DataRecyclerView) view.findViewById(R.id.rv_data);
        dataRecyclerView.setLayoutManager(layoutManager_data);

        daysRecyclerView = (DaysRecyclerView) view.findViewById(R.id.rv_days);
        daysRecyclerView.setLayoutManager(layoutManager_days);

        dataVerticalScrollView = (DataVerticalScrollView) view.findViewById(R.id.data_vertical_scroll);

        areasScrollView = (AreasScrollView) view.findViewById(R.id.areas_scroll);

        areasScrollView.synchronizeScrollWith(dataVerticalScrollView);
        dataVerticalScrollView.synchronizeScrollingWith(areasScrollView);

        dataRecyclerView.synchronizeScrollingWith(daysRecyclerView);
        daysRecyclerView.synchronizeScrollingWith(dataRecyclerView);

        List<Area> areas = ((MainActivity)getActivity()).areas;
        // fake data
        if(areas.size() == 0) {
            areas.add(new Area("Area 1", "dgdsfgsd"));
            areas.add(new Area("Area 2", "fsdgdsfgsd"));
            areas.add(new Area("Area 3", "dgdsfgsd"));
            areas.add(new Area("Area 4", "fsdgdsfgsd"));
            areas.add(new Area("Area 5", "dgdsfgsd"));
            areas.add(new Area("Area 6", "fsdgdsfgsd"));
/*            areas.add(new Area("Area 7", "dgdsfgsd"));
            areas.add(new Area("Area 8", "fsdgdsfgsd"));
            areas.add(new Area("Area 9", "dgdsfgsd"));
            areas.add(new Area("Area 10", "fsdgdsfgsd"));
            areas.add(new Area("Area 11", "dgdsfgsd"));
            areas.add(new Area("Area 12", "fsdgdsfgsd"));
            areas.add(new Area("Area 13", "dgdsfgsd"));
            areas.add(new Area("Area 14", "fsdgdsfgsd"));
            areas.add(new Area("Area 15", "dgdsfgsd"));
            areas.add(new Area("Area 16", "fsdgdsfgsd"));*/
        }

        areasAdapter = new AreasAdapter(getContext(), areas, areasContainer);

        AREAS_COUNT = areas.size();

        List<Day> days = ((MainActivity)getActivity()).days;

        daysAdapter = new DaysAdapter(days);
        daysRecyclerView.setAdapter(daysAdapter);

        dataAdapter = new DataAdapter(days, areas, (MainActivity) getActivity());
        dataRecyclerView.setAdapter(dataAdapter);
        if(days.size() == 0) {
            dataAdapter.loadInitialDaysData(daysAdapter);
            /*for (int i = 0; i < STARTING_DAYS_COUNT; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                calendar.add(Calendar.DATE, i - 29);

                Day d = new Day(calendar);

                days.add(d);
            }*/
        }
        dataRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager_data) {
            @Override
            public void onScrollForwardLoadMore(final EndlessRecyclerViewScrollListener scrollListener) {
/*
                new AsyncTask<Void, Void, Void>(){

                        @Override
                        protected Void doInBackground(Void... voids) {*/
                            dataAdapter.loadNextDaysData(daysAdapter, scrollListener, UPLOAD_DAYS_ON_SCROLL_COUNT);


/*                        return null;
                    }
                }.execute();*/
            }

            @Override
            public void onScrollBackwardLoadMore(final EndlessRecyclerViewScrollListener scrollListener) {
/*                new AsyncTask<Void, Void, Void>(){

                    @Override
                    protected Void doInBackground(Void... voids) {*/
                        dataAdapter.loadPreviousDaysData(daysAdapter, scrollListener, UPLOAD_DAYS_ON_SCROLL_COUNT);
/*
                        return null;
                    }
                }.execute();*/
            }
        };
        dataRecyclerView.addOnScrollListener(dataRecyclerViewScrollListener);
        dataRecyclerViewScrollListener.scrollToToday();

        buttonAddArea = (ImageView) view.findViewById(R.id.button_add_area);
        buttonAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).displaySelectedScreen(R.id.add_area, null);
            }
        });

        return view;
    }

    private void loadNextDaysData(RecyclerView view){
        List<Day> days = ((MainActivity)getActivity()).days;
        Calendar lastDate = days.get(days.size() - 1).getDate();

        if(days.size() > STARTING_DAYS_COUNT) {
            for (int i = 0; i < UPLOAD_DAYS_ON_SCROLL_COUNT; i++) {
                days.remove(0);
            }

            view.post(new Runnable() {
                @Override
                public void run() {
                    dataAdapter.notifyItemRangeRemoved(0, UPLOAD_DAYS_ON_SCROLL_COUNT);
                    daysAdapter.notifyItemRangeRemoved(0, UPLOAD_DAYS_ON_SCROLL_COUNT);
                }
            });

        }

        final int finalCurSize = dataAdapter.getItemCount();

        for(int i = 0; i < UPLOAD_DAYS_ON_SCROLL_COUNT; i++){
            Calendar newDate = new GregorianCalendar(
                    lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);


            newDate.add(Calendar.DATE, i + 1);

            Day d = new Day(newDate);

            days.add(d);
        }


        final int daysSize = days.size();
        view.post(new Runnable() {
            @Override
            public void run() {
                dataAdapter.notifyItemRangeInserted(finalCurSize, daysSize - 1);
                daysAdapter.notifyItemRangeInserted(finalCurSize, daysSize - 1);
            }
        });

        dataRecyclerViewScrollListener.resetState();
    }

    private void loadPreviousDaysData(RecyclerView view){
        List<Day> days = ((MainActivity)getActivity()).days;
        Calendar lastDate = days.get(0).getDate();


        if(days.size() > STARTING_DAYS_COUNT) {
            int beginIndex = days.size() - 1;
            for (int i = beginIndex; i >= beginIndex - UPLOAD_DAYS_ON_SCROLL_COUNT; i--) {
                days.remove(i);
            }

            final int sizeAfterRemoving = days.size();

            view.post(new Runnable() {
                @Override
                public void run() {
                    dataAdapter.notifyItemRangeRemoved(sizeAfterRemoving, UPLOAD_DAYS_ON_SCROLL_COUNT);
                    daysAdapter.notifyItemRangeRemoved(sizeAfterRemoving, UPLOAD_DAYS_ON_SCROLL_COUNT);
                }
            });

        }

        //dataRecyclerViewScrollListener.resetState();

        for(int i = 0; i < UPLOAD_DAYS_ON_SCROLL_COUNT; i++){
            Calendar newDate = new GregorianCalendar(
                    lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);


            newDate.add(Calendar.DATE, - 1 - i);

            Day d = new Day(newDate);
            //days.add
            days.add(0, d);
        }

        view.post(new Runnable() {
            @Override
            public void run() {
                dataAdapter.notifyItemRangeInserted(0, UPLOAD_DAYS_ON_SCROLL_COUNT);
                daysAdapter.notifyItemRangeInserted(0, UPLOAD_DAYS_ON_SCROLL_COUNT);
            }
        });

        dataRecyclerViewScrollListener.resetState();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ((MainActivity)getActivity()).setShortAppBar(mShortToolbar);
    }

    @Override
    public void onDestroyView() {
        ((MainActivity)getActivity()).setLongAppBar(mShortToolbar);

        super.onDestroyView();
    }

}