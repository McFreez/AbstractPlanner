package com.abstractplanner.adapters;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataAdapter extends RecyclerView.Adapter<DataAdapter.DataViewHolder> {

    private static final String TAG = DataAdapter.class.getSimpleName();

    private List<Day> mDays;
    private List<Area> mAreas;
    private MainActivity mActivity;

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
    public int getItemCount() {
        return mDays.size();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = mActivity.getResources().getDisplayMetrics();
        return Math.round(dp * displayMetrics.density);
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
