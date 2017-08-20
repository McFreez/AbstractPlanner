package com.abstractplanner.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.abstractplanner.R;
import com.abstractplanner.dto.Day;
import com.abstractplanner.table.DaysRecyclerView;
import com.abstractplanner.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class DaysAdapter extends RecyclerView.Adapter<DaysAdapter.DaysViewHolder> {

    private static final String TAG = DaysAdapter.class.getSimpleName();

    private List<Day> mDays;
    private Context mContext;
    private DaysRecyclerView mRecyclerView;

    public DaysAdapter(List<Day> days){
        mDays = days;
    }

    @Override
    public DaysViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext = parent.getContext();
        int layoutId = R.layout.days_item;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutId, parent, shouldAttachToParentImmediately);
        DaysViewHolder viewHolder = new DaysViewHolder(view);

        return viewHolder;
    }

    public int getCurrentDayPosition(){
        Calendar today = DateTimeUtils.getTodayDate();

        for(int i = 0; i < mDays.size(); i++){
            if(mDays.get(i).getDate().compareTo(today) == 0)
                return i;
        }

        return -1;
    }

    @Override
    public void onBindViewHolder(DaysViewHolder holder, int position) {
        holder.bind(mDays.get(position).getDate());
        holder.dayTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mRecyclerView.smoothScrollToToday();
            }
        });
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = (DaysRecyclerView) recyclerView;
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

            Calendar today = DateTimeUtils.getTodayDate();

            if(date.compareTo(today) == 0){
                dayTitle.setBackgroundColor(mContext.getResources().getColor(R.color.calendar_background));
                dayTitle.setTextColor(mContext.getResources().getColor(R.color.colorPrimary));
            }
            else {
                dayTitle.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimary));
                dayTitle.setTextColor(mContext.getResources().getColor(R.color.textColor));
            }

            if(date.after(previousYear) && date.before(nextYear))
                dayTitle.setText(DateUtils.formatDateTime(mContext, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE));
            else
                dayTitle.setText(DateUtils.formatDateTime(mContext, date.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR));
        }
    }
}
