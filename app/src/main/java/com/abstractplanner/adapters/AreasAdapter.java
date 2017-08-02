package com.abstractplanner.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;
import com.abstractplanner.fragments.EditAreaDialogFragment;
import com.abstractplanner.fragments.EditTaskDialogFragment;

import java.util.ArrayList;
import java.util.List;

public class AreasAdapter {

    private MainActivity mActivity;
    private List<Area> mAreas;
    private List<View> mAreasViews;
    private LinearLayout mAreasContainer;
    private AbstractPlannerDatabaseHelper mDbHelper;
    //private DataAdapter mDataAdapter;

    public AreasAdapter(MainActivity activity, List<Area> areas, LinearLayout attributesContainer){
        mActivity = activity;
        mAreas = areas;
        mAreasContainer = attributesContainer;
        //mDataAdapter = dataAdapter;

        mDbHelper = mActivity.getDbHelper();

        createViews();
    }

    private void createViews(){
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        mAreasViews = new ArrayList<>();

        for(int i = 0; i < mAreas.size(); i++){

            View view = inflater.inflate(R.layout.areas_item, null, false);
            //int width = (int) mActivity.getResources().getDimension(R.dimen.areas_column_width);
            int height = (int) mActivity.getResources().getDimension(R.dimen.calendar_item_size);
            //view.setLayoutParams(new LinearLayout.LayoutParams(, heightViewGroup.LayoutParams.MATCH_PARENT));

            AreasViewHolder viewHolder = new AreasViewHolder();
            viewHolder.areaTitle = (TextView) view.findViewById(R.id.tv_area);
            viewHolder.areaTitle.setText(mAreas.get(i).getName());


            //viewHolder.areaTitle.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //viewHolder.areaTitle.setBackgroundColor(mActivity.getResources().getColor(R.color.colorPrimary));

            final Area area = mAreas.get(i);

            view.setTag(viewHolder);
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                    builder.setMessage(area.getDescription())
                            .setTitle(area.getName())
                            .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                                    EditAreaDialogFragment newFragment = new EditAreaDialogFragment();
                                    newFragment.setPrevoisArea(area);
                                    newFragment.setAreasAdapter(AreasAdapter.this);
                                    // The device is smaller, so show the fragment fullscreen
                                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                                    // For a little polish, specify a transition animation
                                    transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                    // To make it fullscreen, use the 'content' root view as the container
                                    // for the fragment, which is always the root view for the activity
                                    transaction.add(R.id.drawer_layout, newFragment)
                                            .addToBackStack(null).commit();
                                }
                            })
                            .setPositiveButton("Archive", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    /*dialogInterface.dismiss();*/
                                }
                            })
                            .setNeutralButton("Delete", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    removeArea(area);
                                }
                            });

                    builder.show();
                    return false;
                }
            });
            mAreasContainer.addView(view);
            mAreasViews.add(view);
        }
    }

    private void removeArea(Area area){

        int removeIndex = -1;
        long removeID = -1;

        for (int i = 0; i < mAreas.size(); i++){
            if(mAreas.get(i).equals(area)){
                removeIndex = i;
                removeID = mAreas.get(i).getId();
                break;
            }
        }

        if(removeIndex == -1)
            return;

        mAreas.remove(removeIndex);
        mDbHelper.deleteArea(removeID);

        mActivity.displaySelectedScreen(R.id.calendar_grid, null);
    }

    public void saveEditedArea(Area previousArea, Area newArea){
        for(int i = 0; i < mAreas.size(); i++){
            if(mAreas.get(i).equals(previousArea)){
                mAreas.get(i).setName(newArea.getName());
                mAreas.get(i).setDescription(newArea.getDescription());
                ((AreasViewHolder) mAreasViews.get(i).getTag()).areaTitle.setText(newArea.getName());
                break;
            }
        }
    }

    static class AreasViewHolder {
        public TextView areaTitle;
    }
}
