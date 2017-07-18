package com.abstractplanner.table;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.abstractplanner.adapters.DaysAdapter;

public class DaysRecyclerView extends RecyclerView {

    private boolean followedScrolling = false;

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

    public void scrollToToday(){
        dataView.scrollToToday();
    }

    public DaysAdapter getDaysAdapter(){
        return (DaysAdapter) getAdapter();
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(dataView != null && !followedScrolling)
            dataView.followedScrollTo(dx, dy);

        followedScrolling = false;

    }
}
