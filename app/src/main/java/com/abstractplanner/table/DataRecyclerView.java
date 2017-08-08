package com.abstractplanner.table;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.fragments.CalendarGridFragment;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Calendar;

public class DataRecyclerView extends RecyclerView {

    private static final String LOG_TAG = "DataRecyclerView";

    private boolean followedScrolling = false;
    private boolean followedScrollingToPosition = false;

    private DaysRecyclerView daysView;
    private EndlessRecyclerViewScrollListener mScrollListener;

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

    public void followedScrollToPosition(int position){
        CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();
        followedScrollingToPosition = true;
        layoutManager.scrollToPositionWithOffset(position, 0);
        //scrollToPosition(position);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(followedScrollingToPosition)
        {
            followedScrollingToPosition = false;
            return;
        }

        if(daysView != null && !followedScrolling)
            daysView.followedScrollTo(dx, dy);

        followedScrolling = false;
    }

    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        super.addOnScrollListener(listener);

        mScrollListener = (EndlessRecyclerViewScrollListener) listener;
    }

    public void smoothScrollToToday(){
        DataAdapter adapter = (DataAdapter) getAdapter();
        CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        int todayPosition = adapter.getCurrentDayPosition();
        if(todayPosition >= 0) {
            smoothScrollToPosition(todayPosition);
            /*Log.e(LOG_TAG, "Scrolled normally to position " + todayPosition);*/
        } else {
            final Calendar earliestDayInRange = adapter.getDayByPosition(0).getDate();
            final Calendar latestDayInRange = adapter.getDayByPosition(adapter.getItemCount() - 1).getDate();

            if(today.before(earliestDayInRange)){
                // BEFORE
                int days = Days.daysBetween(new DateTime(today), new DateTime(earliestDayInRange)).getDays();
                if(Math.abs(days) < CalendarGridFragment.STARTING_DAYS_COUNT - EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2){
                    adapter.loadPreviousDaysData(daysView.getDaysAdapter(), mScrollListener, days + EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2);
                    /*Log.e(LOG_TAG, "Scrolled with difference of " + days + " days");*/
                    adapter.notifyDataSetChanged();
                    daysView.getAdapter().notifyDataSetChanged();
                    smoothScrollToToday();
                } else {
                    adapter.loadInitialDaysData(daysView.getDaysAdapter());
                    mScrollListener.resetState();
                    smoothScrollToToday();
                }
            }
            else
                if(today.after(latestDayInRange)){
                // AFTER
                    int days = Days.daysBetween(new DateTime(today), new DateTime(latestDayInRange)).getDays() * (-1);
                    if(Math.abs(days) < CalendarGridFragment.STARTING_DAYS_COUNT - EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2){
                        adapter.loadNextDaysData(daysView.getDaysAdapter(), mScrollListener, days + EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2);
                        /*Log.e(LOG_TAG, "Scrolled with difference of " + days + " days");*/
                        smoothScrollToToday();
                    } else {
                        adapter.loadInitialDaysData(daysView.getDaysAdapter());
                        mScrollListener.resetState();
                        smoothScrollToToday();
                    }
                }
        }
    }

    public void refreshView(){
        DataAdapter adapter = (DataAdapter) getAdapter();
        CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();

        this.setAdapter(null);
        this.setLayoutManager(null);
        this.setAdapter(adapter);
        this.setLayoutManager(layoutManager);
        adapter.notifyDataSetChanged();
    }

    public void scrollToToday(){
        DataAdapter adapter = (DataAdapter) getAdapter();

        int todayPosition = adapter.getCurrentDayPosition();
        if(todayPosition >= 0) {
            //layoutManager.scrollToPositionWithOffset(todayPosition, 0);
            followedScrollToPosition(todayPosition);
            daysView.followedScrollToPosition(todayPosition);
            smoothScrollToPosition(todayPosition);
        }
    }
}