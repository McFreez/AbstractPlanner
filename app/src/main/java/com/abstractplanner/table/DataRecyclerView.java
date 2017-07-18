package com.abstractplanner.table;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.abstractplanner.adapters.DataAdapter;
import com.abstractplanner.fragments.CalendarGridFragment;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;

import java.util.Calendar;

public class DataRecyclerView extends RecyclerView {

    private static final String LOG_TAG = "DataRecyclerView";

    private boolean followedScrolling = false;

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

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);

        if(daysView != null && !followedScrolling)
            daysView.followedScrollTo(dx, dy);

        followedScrolling = false;
    }

    @Override
    public void addOnScrollListener(OnScrollListener listener) {
        super.addOnScrollListener(listener);

        mScrollListener = (EndlessRecyclerViewScrollListener) listener;
    }

    public void scrollToToday(){
        DataAdapter adapter = (DataAdapter) getAdapter();
        CenterLayoutManager layoutManager = (CenterLayoutManager) getLayoutManager();
        int middleItemPosition = (layoutManager.findLastVisibleItemPosition() - layoutManager.findFirstVisibleItemPosition()) / 2 + 1;

        int todayPosition = adapter.getCurrentDayPosition();
        if(todayPosition >= 0/*middleItemPosition && todayPosition < getAdapter().getItemCount() - middleItemPosition*/) {
            smoothScrollToPosition(todayPosition);
            Log.e(LOG_TAG, "Scrolled normally to position " + todayPosition);
        } else {
            /*final Calendar earliestDayInRange = adapter.getDayByPosition(0).getDate();
            final Calendar latestDayInRange = adapter.getDayByPosition(adapter.getItemCount() - 1).getDate();
            Calendar today = Calendar.getInstance();
            today.set(Calendar.HOUR_OF_DAY, 0);
            today.set(Calendar.MINUTE, 0);
            today.set(Calendar.SECOND, 0);
            today.set(Calendar.MILLISECOND, 0);
            if(today.before(earliestDayInRange)){
                // BEFORE
                int days = Days.daysBetween(new DateTime(today), new DateTime(earliestDayInRange)).getDays();
                if(Math.abs(days) < CalendarGridFragment.STARTING_DAYS_COUNT - EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2){
                    adapter.loadPreviousDaysData(daysView.getDaysAdapter(), mScrollListener, days + EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2);
                    Log.e(LOG_TAG, "Scrolled with difference of " + days + " days");
                    scrollToToday();
                    //scrollToToday();
                } else {
                    Log.e(LOG_TAG, "Reinitializing data");
                    adapter.goBackwardToInitialData(daysView.getDaysAdapter(), mScrollListener);
                }
            }
            else
                if(today.after(latestDayInRange)){
                // AFTER
                    int days = Days.daysBetween(new DateTime(today), new DateTime(latestDayInRange)).getDays() * (-1);
                    if(Math.abs(days) < CalendarGridFragment.STARTING_DAYS_COUNT - EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2){
                        adapter.loadNextDaysData(daysView.getDaysAdapter(), mScrollListener, days + EndlessRecyclerViewScrollListener.VISIBLE_THRESHOLD * 2);
                        Log.e(LOG_TAG, "Scrolled with difference of " + days + " days");
                        scrollToToday();
                        //scrollToToday();
                    } else {
                        Log.e(LOG_TAG, "Reinitializing data");
                        adapter.goForwardToInitialData(daysView.getDaysAdapter(), mScrollListener);
                    }
                }*/
        }

    }
}

