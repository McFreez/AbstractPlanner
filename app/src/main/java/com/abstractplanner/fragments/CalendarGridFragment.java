package com.abstractplanner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.adapters.AreasAdapter;
import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.adapters.DaysAdapter;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;
import com.abstractplanner.table.AreasScrollView;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.DataVerticalScrollView;
import com.abstractplanner.table.DaysRecyclerView;
import com.abstractplanner.table.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Random;

public class CalendarGridFragment extends Fragment{

    private static final String LOG_TAG = "CalendarGridFragment";

    private Toolbar mShortToolbar;
    private static int AREAS_COUNT;
    private static final int DAYS_COUNT = 10;

    private AreasAdapter areasAdapter;
    private DaysAdapter daysAdapter;
    private DataAdapter dataAdapter;

    private AreasScrollView areasScrollView;
    private LinearLayout areasContainer;
    private DataRecyclerView dataRecyclerView;
    private EndlessRecyclerViewScrollListener dataRecyclerViewScrollListener;
    private DaysRecyclerView daysRecyclerView;
    private EndlessRecyclerViewScrollListener daysRecyclerViewScrollListener;
    private DataVerticalScrollView dataVerticalScrollView;

    private ImageView buttonAddArea;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_grid, container, false);

        mShortToolbar = (Toolbar) view.findViewById(R.id.toolbar_short);

        final LinearLayoutManager layoutManager_data = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
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
/*        if(areas.size() == 0) {
            areas.add(new Area("Area 1", "dgdsfgsd"));
            areas.add(new Area("Area 2", "fsdgdsfgsd"));
            areas.add(new Area("Area 3", "dgdsfgsd"));
            areas.add(new Area("Area 4", "fsdgdsfgsd"));
            areas.add(new Area("Area 5", "dgdsfgsd"));
            areas.add(new Area("Area 6", "fsdgdsfgsd"));
            areas.add(new Area("Area 7", "dgdsfgsd"));
            areas.add(new Area("Area 8", "fsdgdsfgsd"));
            areas.add(new Area("Area 9", "dgdsfgsd"));
            areas.add(new Area("Area 10", "fsdgdsfgsd"));
            areas.add(new Area("Area 11", "dgdsfgsd"));
            areas.add(new Area("Area 12", "fsdgdsfgsd"));
            areas.add(new Area("Area 13", "dgdsfgsd"));
            areas.add(new Area("Area 14", "fsdgdsfgsd"));
            areas.add(new Area("Area 15", "dgdsfgsd"));
            areas.add(new Area("Area 16", "fsdgdsfgsd"));
        }*/

        areasAdapter = new AreasAdapter(getContext(), areas, areasContainer);

        AREAS_COUNT = areas.size();

        List<Day> days = ((MainActivity)getActivity()).days;
        if(days.size() == 0) {
            //Calendar calendar = Calendar.getInstance();
            for (int i = 0; i < DAYS_COUNT; i++) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                if(i > 0)
                    calendar.add(Calendar.DATE, i);

                Day d = new Day(calendar);

                days.add(d);
            }
        }

        daysAdapter = new DaysAdapter(days);
        daysRecyclerView.setAdapter(daysAdapter);

        dataAdapter = new DataAdapter(days, areas, (MainActivity) getActivity());
        dataRecyclerView.setAdapter(dataAdapter);
        dataRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(layoutManager_data) {
            @Override
            public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                /*List<Day> days = ((MainActivity)getActivity()).days;
                Calendar lastDate = days.get(days.size() - 1).getDate();
                int curSize = days.size();
                //lastDate = days.get(days.size() - 1).getDate();

                //days.clear();

                if(days.size() >= 40) {
                    for (int i = 0; i < 30; i++)
                        days.remove(0);
                    curSize = days.size();

                *//*dataAdapter.notifyDataSetChanged();*//*
                    //dataAdapter.notifyItemRangeRemoved(0, 30 - 1);
                *//*daysAdapter.notifyDataSetChanged();*//*
                    //dataAdapter.notifyItemRangeRemoved(0, 30 - 1);

                    dataAdapter.notifyDataSetChanged();
                    daysAdapter.notifyDataSetChanged();

                }


                dataRecyclerViewScrollListener.resetState();

                for(int i = 0; i < 30; i++){
                    Calendar newDate = new GregorianCalendar(
                            lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
                    newDate.set(Calendar.HOUR_OF_DAY, 0);
                    newDate.set(Calendar.MINUTE, 0);
                    newDate.set(Calendar.SECOND, 0);
                    newDate.set(Calendar.MILLISECOND, 0);

                    if(i > 0)
                        newDate.add(Calendar.DATE, i);

                    Day d = new Day(newDate);

                    days.add(d);
                }

*//*                for(int i = 0; i < days.size(); i++){
                    Log.e(LOG_TAG, DateUtils.formatDateTime(getContext(), days.get(i).getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                }*//*
                //Log.e(LOG_TAG, "" + days.size());


                dataAdapter.notifyDataSetChanged();
                daysAdapter.notifyDataSetChanged();

                int daysLast = layoutManager_days.findLastVisibleItemPosition();
                int daysFirst = layoutManager_days.findFirstVisibleItemPosition();

                int dataLast = layoutManager_data.findLastVisibleItemPosition();
                int dataFirst = layoutManager_data.findFirstVisibleItemPosition();

                daysRecyclerView.scrollToPosition(0);
                //dataRecyclerView.scrollToPosition(35);
*//*                dataAdapter.notifyItemRangeInserted(curSize, 30);
                daysAdapter.notifyItemRangeInserted(curSize, 30);*//*
*//*                List<Day> days = ((MainActivity)getActivity()).days;
                Calendar lastDate = days.get(days.size() - 1).getDate();
                //lastDate = days.get(days.size() - 1).getDate();
                Calendar newDate = new GregorianCalendar(
                        lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
                newDate.add(Calendar.DATE, 1);
                //days.remove(0);
                days.add(new Day(newDate));
                for(int i = 0; i < days.size(); i++){
                    Log.e(LOG_TAG, DateUtils.formatDateTime(getContext(), days.get(i).getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
                }*//*
                //daysAdapter.notifyDataSetChanged();
                //dataAdapter.notifyDataSetChanged();
                int i = 0;*/
            }
        };
        dataRecyclerView.addOnScrollListener(dataRecyclerViewScrollListener);
        daysRecyclerView.addOnScrollListener(dataRecyclerViewScrollListener);

        buttonAddArea = (ImageView) view.findViewById(R.id.button_add_area);
        buttonAddArea.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).displaySelectedScreen(R.id.add_area, null);
            }
        });

        return view;
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
