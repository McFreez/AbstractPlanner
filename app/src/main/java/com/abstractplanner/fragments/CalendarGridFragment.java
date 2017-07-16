package com.abstractplanner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;
import com.abstractplanner.table.AreasScrollView;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.DataVerticalScrollView;
import com.abstractplanner.table.DaysRecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class CalendarGridFragment extends Fragment{

    private Toolbar mShortToolbar;
    private static int AREAS_COUNT;
    private static final int DAYS_COUNT = 10;

    private AreasAdapter areasAdapter;
    private DaysAdapter daysAdapter;
    private DataAdapter dataAdapter;

    private AreasScrollView areasScrollView;
    private LinearLayout areasContainer;
    private DataRecyclerView dataRecyclerView;
    private DaysRecyclerView daysRecyclerView;
    private DataVerticalScrollView dataVerticalScrollView;

    private ImageView buttonAddArea;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_grid, container, false);

        mShortToolbar = (Toolbar) view.findViewById(R.id.toolbar_short);

        LinearLayoutManager layoutManager_data = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager_days = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

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
        }
*/

        areasAdapter = new AreasAdapter(getContext(), areas, areasContainer);

        AREAS_COUNT = areas.size();

        List<Day> days = ((MainActivity)getActivity()).days;
        if(days.size() == 0) {
            for (int i = 0; i < DAYS_COUNT; i++) {
                Day d = new Day("Day " + i);
                // fake data
/*                Random r = new Random();
                int areaIndex = r.nextInt(areas.size());
                Task t = new Task(areas.get(areaIndex), "Name " + i, "sdgsgsd fg sd gsd f");
                t.setDone(r.nextBoolean());
                d.addTask(t);
                */

                days.add(d);
            }
        }

        daysAdapter = new DaysAdapter(days);
        daysRecyclerView.setAdapter(daysAdapter);

        dataAdapter = new DataAdapter(days, areas, (MainActivity) getActivity());
        dataRecyclerView.setAdapter(dataAdapter);

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
