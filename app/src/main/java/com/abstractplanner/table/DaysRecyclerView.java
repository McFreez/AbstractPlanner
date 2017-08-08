package com.abstractplanner.table;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.abstractplanner.adapters.DaysAdapter;

public class DaysRecyclerView extends RecyclerView {

    private boolean followedScrolling = false;
    private boolean followedScrollingToPosition = false;

    private DataRecyclerView dataView;

    public DaysRecyclerView(Context context) {
        super(context);
    }

    public DaysRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DaysRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void synchronizeScrollingWith(DataRecyclerView recyclerView){
        dataView = recyclerView;
    }

    public void followedScrollTo(int dx, int dy){
        followedScrolling = true;
        scrollBy(dx, dy);
    }

    public void followedScrollToPosition(int position){
        CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();
        followedScrollingToPosition = true;
        layoutManager.scrollToPositionWithOffset(position, 0);
        scrollToPosition(position);
    }

    public DaysAdapter getDaysAdapter(){
        return (DaysAdapter) getAdapter();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(followedScrollingToPosition)
        {
            followedScrollingToPosition = false;
            return;
        }

        if(dataView != null && !followedScrolling)
            dataView.followedScrollTo(dx, dy);

        followedScrolling = false;
    }

    public void smoothScrollToToday(){
        dataView.smoothScrollToToday();
    }

    public void scrollToToday(){
        DaysAdapter adapter = (DaysAdapter) getAdapter();
        //CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();

        int todayPosition = adapter.getCurrentDayPosition();
        if(todayPosition >= 0) {
            //layoutManager.scrollToPositionWithOffset(todayPosition, 0);
            followedScrollToPosition(todayPosition);
            //daysView.followedScrollToPosition(todayPosition);
            //smoothScrollToPosition(todayPosition);
        }
    }
}
