package com.abstractplanner.table;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class DataVerticalScrollView extends ScrollView {

    private boolean followedScrolling = false;

    private AreasScrollView areasScrollView;

    public DataVerticalScrollView(Context context) {
        super(context);
    }

    public DataVerticalScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DataVerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DataVerticalScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void synchronizeScrollingWith(AreasScrollView scrollView){
        areasScrollView = scrollView;
    }

    public void followedScrollTo(int x, int y){
        followedScrolling = true;
        scrollTo(x, y);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if(areasScrollView != null && !followedScrolling)
            areasScrollView.followedScrollTo(l, t);

        followedScrolling = false;
    }



}
