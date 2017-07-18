package com.abstractplanner.table;

import android.support.v7.widget.RecyclerView;

public abstract class EndlessRecyclerViewScrollListener extends RecyclerView.OnScrollListener {

    // The minimum amount of items to have below your current scroll position
    // before loading more.
    public static final int VISIBLE_THRESHOLD = 10;
    // The current offset index of data you have loaded
    private int currentPage = 0;
    // The total number of items in the dataset after the last load
    private int previousTotalItemCount = 0;
    // True if we are still waiting for the last set of data to load.
    private boolean loading = true;
    // Sets the starting page index
    private int startingPageIndex = 0;

    private boolean scrollToTodayRequested = false;

    RecyclerView.LayoutManager mLayoutManager;

    public EndlessRecyclerViewScrollListener(CenterLayoutManager layoutManager) {
        this.mLayoutManager = layoutManager;
    }

    // This happens many times a second during a scroll, so be wary of the code you place here.
    // We are given a few useful parameters to help us work out if we need to load some more data,
    // but first we check if we are waiting for the previous load to finish.
    @Override
    public void onScrolled(RecyclerView view, int dx, int dy) {
        //int lastVisibleItemPosition = 0;
        int totalItemCount = mLayoutManager.getItemCount();

        if(scrollToTodayRequested) {
            ((DataRecyclerView) view).scrollToToday();
            scrollToTodayRequested = false;
        }

        // If the total item count is zero and the previous isn't, assume the
        // list is invalidated and should be reset back to initial state
        if (totalItemCount < previousTotalItemCount) {
            this.currentPage = this.startingPageIndex;
            this.previousTotalItemCount = totalItemCount;
            if (totalItemCount == 0) {
                this.loading = true;
            }
        }
        // If it’s still loading, we check to see if the dataset count has
        // changed, if so we conclude it has finished loading and update the current page
        // number and total item count.
        if (loading && (totalItemCount > previousTotalItemCount)) {
            loading = false;
            previousTotalItemCount = totalItemCount;
        }

        if(dx > 0) {
            int lastVisibleItemPosition = ((CenterLayoutManager) mLayoutManager).findLastVisibleItemPosition();

            // If it isn’t currently loading, we check to see if we have breached
            // the VISIBLE_THRESHOLD and need to reload more data.
            // If we do need to reload some more data, we execute onScrollForwardLoadMore to fetch the data.
            // threshold should reflect how many total columns there are too
            if (!loading && (lastVisibleItemPosition + VISIBLE_THRESHOLD) > totalItemCount) {
                currentPage++;
                onScrollForwardLoadMore(this);
                loading = true;
            }
        }
        else
            if(dx < 0){
                int firstVisibleItemPosition = ((CenterLayoutManager) mLayoutManager).findFirstVisibleItemPosition();
                // If it isn’t currently loading, we check to see if we have breached
                // the VISIBLE_THRESHOLD and need to reload more data.
                // If we do need to reload some more data, we execute onScrollForwardLoadMore to fetch the data.
                // threshold should reflect how many total columns there are too
                if (!loading && firstVisibleItemPosition < VISIBLE_THRESHOLD) {
                    if(currentPage > 0)
                        currentPage--;
                    onScrollBackwardLoadMore(this);
                    loading = true;
                }
            }
    }

    public void scrollToToday(){
        scrollToTodayRequested = true;
    }

    // Call this method whenever performing new searches
    public void resetState() {
        this.currentPage = this.startingPageIndex;
        this.previousTotalItemCount = 0;
        this.loading = true;
    }

    // Defines the process for actually loading more data based on page
    //public abstract void onScrollForwardLoadMore(int page, int totalItemsCount, RecyclerView view);
    public abstract void onScrollForwardLoadMore(EndlessRecyclerViewScrollListener scrollListener);

    public abstract void onScrollBackwardLoadMore(EndlessRecyclerViewScrollListener scrollListener);
}
