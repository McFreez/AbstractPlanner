package com.abstractplanner.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
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
import com.abstractplanner.adapters.AttributesAdapter;
import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.adapters.DaysAdapter;
import com.abstractplanner.dto.Attribute;
import com.abstractplanner.table.AttributesScrollView;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.DataVerticalScrollView;
import com.abstractplanner.table.DaysRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class CalendarGridFragment extends Fragment{

    private Toolbar mShortToolbar;
    private static int ATTRIBUTES_COUNT;
    private static final int DAYS_COUNT = 10;

    private AttributesAdapter attributesAdapter;
    private DaysAdapter daysAdapter;
    private DataAdapter dataAdapter;

    private AttributesScrollView attributesScrollView;
    private LinearLayout attributesContainer;
    private DataRecyclerView dataRecyclerView;
    private DaysRecyclerView daysRecyclerView;
    private DataVerticalScrollView dataVerticalScrollView;

    private ImageView buttonAddAttribute;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_calendar_grid, container, false);

        mShortToolbar = (Toolbar) view.findViewById(R.id.toolbar_short);

        LinearLayoutManager layoutManager_data = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        LinearLayoutManager layoutManager_days = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);

        attributesContainer = (LinearLayout) view.findViewById(R.id.attributes_container);

        dataRecyclerView = (DataRecyclerView) view.findViewById(R.id.rv_data);
        dataRecyclerView.setLayoutManager(layoutManager_data);

        daysRecyclerView = (DaysRecyclerView) view.findViewById(R.id.rv_days);
        daysRecyclerView.setLayoutManager(layoutManager_days);

        dataVerticalScrollView = (DataVerticalScrollView) view.findViewById(R.id.data_vertical_scroll);

        attributesScrollView = (AttributesScrollView) view.findViewById(R.id.attributes_scroll);

        attributesScrollView.synchronizeScrollWith(dataVerticalScrollView);
        dataVerticalScrollView.synchronizeScrollingWith(attributesScrollView);

        dataRecyclerView.synchronizeScrollingWith(daysRecyclerView);
        daysRecyclerView.synchronizeScrollingWith(dataRecyclerView);

        List<Attribute> attributes = ((MainActivity)getActivity()).attributes;
        attributesAdapter = new AttributesAdapter(getContext(), attributes, attributesContainer);

        ATTRIBUTES_COUNT = attributes.size();

        daysAdapter = new DaysAdapter(DAYS_COUNT);
        daysRecyclerView.setAdapter(daysAdapter);

        dataAdapter = new DataAdapter(DAYS_COUNT, ATTRIBUTES_COUNT, getContext());
        dataRecyclerView.setAdapter(dataAdapter);

        buttonAddAttribute = (ImageView) view.findViewById(R.id.button_add_attribute);
        buttonAddAttribute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((MainActivity)getActivity()).displaySelectedScreen(R.id.add_attribute);
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
