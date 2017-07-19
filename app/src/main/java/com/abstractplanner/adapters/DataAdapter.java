package com.abstractplanner.adapters;

import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.abstractplanner.MainActivity;
import com.abstractplanner.R;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.table.CenterLayoutManager;
import com.abstractplanner.table.DataRecyclerView;
import com.abstractplanner.table.EndlessRecyclerViewScrollListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private static final String LOG_TAG = "DataAdapter";

    private List<Day> mDays;
    private List<Area> mAreas;
    private MainActivity mActivity;
    private DataRecyclerView mRecyclerView;

    public DataAdapter(List<Day> days, List<Area> areas, MainActivity activity){
        mDays = days;
        mAreas = areas;
        mActivity = activity;
    }

    @Override
    public DataAdapter.DataViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutId = R.layout.data_in_day_column;
        LayoutInflater inflater = LayoutInflater.from(mActivity);
        boolean shouldAttachToParentImmediately = false;

        View view = inflater.inflate(layoutId, parent, shouldAttachToParentImmediately);
        DataAdapter.DataViewHolder viewHolder = new DataAdapter.DataViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(DataAdapter.DataViewHolder holder, int position) {
        holder.bind(mDays.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = (DataRecyclerView) recyclerView;
    }

    @Override
    public int getItemCount() {
        return mDays.size();
    }

    public int getCurrentDayPosition(){
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        for(int i = 0; i < mDays.size(); i++){
            if(mDays.get(i).getDate().compareTo(today) == 0)
                return i;
        }

        return -1;
    }

    public Day getDayByPosition(int position){
        return mDays.get(position);
    }

    public void loadNextDaysData(final DaysAdapter daysAdapter, EndlessRecyclerViewScrollListener scrollListener, final int addingCount){
        List<Day> days = mActivity.days;
        Calendar lastDate = days.get(days.size() - 1).getDate();

        final int previousSize = this.getItemCount();

        for(int i = 0; i < addingCount; i++){
            Calendar newDate = new GregorianCalendar(
                    lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);

            newDate.add(Calendar.DATE, i + 1);

            Day d = new Day(newDate);

            days.add(d);
        }

        final int daysSize = days.size();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                DataAdapter.this.notifyItemRangeInserted(previousSize, daysSize - 1);
                daysAdapter.notifyItemRangeInserted(previousSize, daysSize - 1);
            }
        });

        if(days.size() > CalendarGridFragment.STARTING_DAYS_COUNT) {
            for (int i = 0; i < addingCount; i++) {
                days.remove(0);
            }

            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    DataAdapter.this.notifyItemRangeRemoved(0, addingCount);
                    daysAdapter.notifyItemRangeRemoved(0, addingCount);
                }
            });

        }

        scrollListener.resetState();

    }

    public void loadPreviousDaysData(final DaysAdapter daysAdapter, EndlessRecyclerViewScrollListener scrollListener, final int addingCount){
        List<Day> days = mActivity.days;
        Calendar lastDate = days.get(0).getDate();

        if(days.size() > CalendarGridFragment.STARTING_DAYS_COUNT) {
            int beginIndex = days.size() - 1;
            for (int i = beginIndex; i >= beginIndex - addingCount; i--) {
                days.remove(i);
            }

            final int sizeAfterRemoving = days.size();

            mRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    DataAdapter.this.notifyItemRangeRemoved(sizeAfterRemoving, addingCount);
                    daysAdapter.notifyItemRangeRemoved(sizeAfterRemoving, addingCount);
                }
            });

        }

        for(int i = 0; i < addingCount; i++){
            Calendar newDate = new GregorianCalendar(
                    lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
            newDate.set(Calendar.HOUR_OF_DAY, 0);
            newDate.set(Calendar.MINUTE, 0);
            newDate.set(Calendar.SECOND, 0);
            newDate.set(Calendar.MILLISECOND, 0);


            newDate.add(Calendar.DATE, - 1 - i);

            Day d = new Day(newDate);
            days.add(0, d);
        }

        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                DataAdapter.this.notifyItemRangeInserted(0, addingCount);
                daysAdapter.notifyItemRangeInserted(0, addingCount);
            }
        });

        scrollListener.resetState();
    }

    public void loadInitialDaysData(DaysAdapter daysAdapter){
        mDays.clear();
        for (int i = 0; i < CalendarGridFragment.STARTING_DAYS_COUNT; i++) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            calendar.add(Calendar.DATE, i - CalendarGridFragment.TODAY_INITIAL_POSITION);

            Day d = new Day(calendar);

            mDays.add(d);
        }

        this.notifyDataSetChanged();
        daysAdapter.notifyDataSetChanged();
    }

    class DataViewHolder extends RecyclerView.ViewHolder{

        LinearLayout container;
        List<View> taskContainers;

        public DataViewHolder(View itemView) {
            super(itemView);

            taskContainers = new ArrayList<>();

            container = (LinearLayout) itemView.findViewById(R.id.data_in_day_container);
            LayoutInflater inflater = LayoutInflater.from(mActivity);

            for(int i = 0; i < mAreas.size(); i++){

                View view = inflater.inflate(R.layout.data_task, null, false);

                DataTaskViewHolder viewHolder = new DataTaskViewHolder();

                viewHolder.area = mAreas.get(i);
                viewHolder.addTaskButton = (ImageView) view.findViewById(R.id.add_task_button);
                viewHolder.taskShortDescriptionContainer = (LinearLayout) view.findViewById(R.id.task_short_description_container);
                viewHolder.taskStatus = (ImageView) view.findViewById(R.id.task_status);
                viewHolder.taskName = (TextView) view.findViewById(R.id.task_name);

                viewHolder.taskShortDescriptionContainer.setBackgroundColor(Color.GREEN);
                viewHolder.addTaskButton.setBackgroundColor(Color.GREEN);



                view.setTag(viewHolder);

                taskContainers.add(view);
                container.addView(view);
            }
        }

        void bind(Day day){

            for(View v : taskContainers){
                DataTaskViewHolder viewHolder = (DataTaskViewHolder) v.getTag();
                final Area area = viewHolder.area;
                final Calendar calendarDate = new GregorianCalendar(
                        day.getDate().get(Calendar.YEAR), day.getDate().get(Calendar.MONTH), day.getDate().get(Calendar.DAY_OF_MONTH));;
                viewHolder.addTaskButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Map<String, Object> additionalData = new HashMap<>();
                        additionalData.put("taskAreaName", area.getName());
                        additionalData.put("taskDay", calendarDate);
                        mActivity.displaySelectedScreen(R.id.add_task, additionalData);
                    }
                });
                viewHolder.taskShortDescriptionContainer.setVisibility(View.GONE);
                viewHolder.addTaskButton.setVisibility(View.VISIBLE);

                for (Task t : day.getTasks()) {
                    if(viewHolder.area.getName().equals(t.getArea().getName())){
                        viewHolder.addTaskButton.setVisibility(View.GONE);
                        viewHolder.taskShortDescriptionContainer.setVisibility(View.VISIBLE);
                        viewHolder.taskName.setText(t.getName());

                        if(t.isDone()){
                            viewHolder.taskStatus.setImageResource(R.drawable.checkbox_marked_circle_outline);
                        }
                        else
                            viewHolder.taskStatus.setImageResource(R.drawable.checkbox_blank_circle_outline);
                    }
                }
            }
        }

        class DataTaskViewHolder {
            Area area;
            ImageView addTaskButton;
            LinearLayout taskShortDescriptionContainer;
            ImageView taskStatus;
            TextView taskName;
        }
    }
}
