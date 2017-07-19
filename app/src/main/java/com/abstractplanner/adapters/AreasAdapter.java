package com.abstractplanner.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abstractplanner.R;
import com.abstractplanner.dto.Area;

import java.util.ArrayList;
import java.util.List;

public class AreasAdapter {

    private Context mContext;
    private List<Area> mAreas;
    private List<View> mAreasViews;
    private LinearLayout mAreasContainer;

    public AreasAdapter(Context context, List<Area> areas, LinearLayout attributesContainer){
        mContext = context;
        mAreas = areas;
        mAreasContainer = attributesContainer;

        createViews();
    }

    private void createViews(){
        LayoutInflater inflater = LayoutInflater.from(mContext);
        mAreasViews = new ArrayList<>();

        for(int i = 0; i < mAreas.size(); i++){

            View view = inflater.inflate(R.layout.areas_item, null, false);

            AreasViewHolder viewHolder = new AreasViewHolder();
            viewHolder.areaTitle = (TextView) view.findViewById(R.id.tv_area);
            viewHolder.areaTitle.setText(mAreas.get(i).getName());
            view.setTag(viewHolder);

            mAreasContainer.addView(view);
            mAreasViews.add(view);
        }
    }

    static class AreasViewHolder {
        public TextView areaTitle;
    }
}
