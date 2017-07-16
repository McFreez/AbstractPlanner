package com.abstractplanner.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abstractplanner.R;
import com.abstractplanner.dto.Day;

import java.util.List;


public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DaysViewHolder> {

    private static final String TAG = DaysAdapter.class.getSimpleName();

    private List<Day> mDays;

    public DaysAdapter(List<Day> days){
        mDays = days;
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
        holder.bind(mDays.get(position).getDate());
    }

    @Override
    public int getItemCount() {
        return mDays.size();
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
