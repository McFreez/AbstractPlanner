package com.abstractplanner.table;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class AttributesScrollView extends ScrollView {

    private boolean followedScrolling = false;

    private DataVerticalScrollView dataVerticalScroll;

    public AttributesScrollView(Context context) {
        super(context);
    }

    public AttributesScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public AttributesScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public AttributesScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void synchronizeScrollWith(DataVerticalScrollView scrollView){
        dataVerticalScroll = scrollView;
    }

    public void followedScrollTo(int x, int y){
        followedScrolling = true;
        scrollTo(x, y);
    }

    @Override
    protected void onScrollChanged(int l, int t, int oldl, int oldt) {
        super.onScrollChanged(l, t, oldl, oldt);

        if(dataVerticalScroll != null && !followedScrolling)
            dataVerticalScroll.followedScrollTo(l, t);

        followedScrolling = false;
    }
}
