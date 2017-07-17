package com.abstractplanner.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abstractplanner.R;
import com.abstractplanner.dto.Day;

import java.util.Calendar;
import java.util.GregorianCalendar;
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

        void bind(Calendar date){
            Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
            Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

            if(date.after(previousYear) && date.before(nextYear))
                dayTitle.setText(String.format("%1$tb %1$te", date));
            else
                dayTitle.setText(String.format("%1$tb %1$te, %1$tY", date));
        }
    }
}
