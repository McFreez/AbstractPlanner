package com.abstractplanner.table;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

public class DataRecyclerView extends RecyclerView {

    private boolean followedScrolling = false;

    private DaysRecyclerView daysView;

    public DataRecyclerView(Context context) {
        super(context);
    }

    public DataRecyclerView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DataRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void synchronizeScrollingWith(DaysRecyclerView recyclerView){
        daysView = recyclerView;
    }

    public void followedScrollTo(int dx, int dy){
        followedScrolling = true;
        scrollBy(dx, dy);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(daysView != null && !followedScrolling)
            daysView.followedScrollTo(dx, dy);

        followedScrolling = false;
    }
}

