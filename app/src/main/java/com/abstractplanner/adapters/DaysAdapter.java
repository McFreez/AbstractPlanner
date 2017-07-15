package com.abstractplanner.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abstractplanner.R;


public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DaysViewHolder> {

    private static final String TAG = DaysAdapter.class.getSimpleName();

    private int mDaysCount;

    public DaysAdapter(int daysCount){
        mDaysCount = daysCount;
    }

    @Override
    public DaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        int layoutId = R.layout.days_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutId, parent, shouldAttachToParentImmediately);
        DaysViewHolder viewHolder = new DaysViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DaysViewHolder holder, int position) {
        holder.bind(String.valueOf("Day " + position));
    }

    @Override
    public int getItemCount() {
        return mDaysCount;
    }

    class DaysViewHolder extends RecyclerView.ViewHolder{

        TextView dayTitle;

        public DaysViewHolder(View itemView) {
            super(itemView);

            dayTitle = (TextView) itemView.findViewById(R.id.tv_day);
        }

        void bind(String title){
            dayTitle.setText(title);
        }
    }
}
