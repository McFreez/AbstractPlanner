package com.abstractplanner.adapters;

import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
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
import com.abstractplanner.data.AbstractPlannerContract.*;
import com.abstractplanner.data.AbstractPlannerDatabaseHelper;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Day;
import com.abstractplanner.dto.Task;
import com.abstractplanner.fragments.CalendarGridFragment;
import com.abstractplanner.fragments.EditTaskDialogFragment;
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
    private AbstractPlannerDatabaseHelper mDbHelper;

    public DataAdapter(List<Day> days, List<Area> areas, MainActivity activity){
        mDays = days;
        mAreas = areas;
        mActivity = activity;
        mDbHelper = mActivity.getDbHelper();
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
        //List<Day> days = mActivity.days;
        Calendar lastDate = mDays.get(mDays.size() - 1).getDate();

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

            mDays.add(d);
        }

        Calendar startDate = new GregorianCalendar(
                lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
        startDate.add(Calendar.DATE, 1);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        Calendar endDate = new GregorianCalendar(
                lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
        endDate.add(Calendar.DATE, addingCount);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        getDaysTasks(startDate, endDate);

        final int daysSize = mDays.size();
        mRecyclerView.post(new Runnable() {
            @Override
            public void run() {
                DataAdapter.this.notifyItemRangeInserted(previousSize, daysSize - 1);
                daysAdapter.notifyItemRangeInserted(previousSize, daysSize - 1);
            }
        });

        if(mDays.size() > CalendarGridFragment.STARTING_DAYS_COUNT) {
            for (int i = 0; i < addingCount; i++) {
                mDays.remove(0);
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
        //List<Day> days = mActivity.days;
        Calendar lastDate = mDays.get(0).getDate();

        if(mDays.size() > CalendarGridFragment.STARTING_DAYS_COUNT) {
            int beginIndex = mDays.size() - 1;
            for (int i = beginIndex; i >= beginIndex - addingCount; i--) {
                mDays.remove(i);
            }

            final int sizeAfterRemoving = mDays.size();

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
            mDays.add(0, d);
        }

        Calendar startDate = new GregorianCalendar(
                lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
        startDate.add(Calendar.DATE, - addingCount);
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);

        Calendar endDate = new GregorianCalendar(
                lastDate.get(Calendar.YEAR), lastDate.get(Calendar.MONTH), lastDate.get(Calendar.DAY_OF_MONTH));
        endDate.add(Calendar.DATE, - 1);
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);

        getDaysTasks(startDate, endDate);

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

        Calendar startDate = Calendar.getInstance();
        startDate.set(Calendar.HOUR_OF_DAY, 0);
        startDate.set(Calendar.MINUTE, 0);
        startDate.set(Calendar.SECOND, 0);
        startDate.set(Calendar.MILLISECOND, 0);
        startDate.add(Calendar.DATE, - CalendarGridFragment.TODAY_INITIAL_POSITION);

        Calendar endDate = Calendar.getInstance();
        endDate.set(Calendar.HOUR_OF_DAY, 0);
        endDate.set(Calendar.MINUTE, 0);
        endDate.set(Calendar.SECOND, 0);
        endDate.set(Calendar.MILLISECOND, 0);
        endDate.add(Calendar.DATE, CalendarGridFragment.STARTING_DAYS_COUNT - CalendarGridFragment.TODAY_INITIAL_POSITION);

        getDaysTasks(startDate, endDate);

        this.notifyDataSetChanged();
        daysAdapter.notifyDataSetChanged();
    }

    private void getDaysTasks(Calendar startDate, Calendar endDate){
        Cursor taskCursor = mDbHelper.getTasksInRange(startDate, endDate);

        for(int i = 0; i < taskCursor.getCount(); i++){
            taskCursor.moveToPosition(i);
            long taskDateMillis = taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_DATE));
            Calendar taskDate = Calendar.getInstance();
            taskDate.setTimeInMillis(taskDateMillis);
            /*Log.e(LOG_TAG, "Day " + i + " is " + DateUtils.formatDateTime(mActivity, mDays.get(i).getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
            + " Task");*/
            for (Day d : mDays){
                if(d.getDate().compareTo(taskDate) == 0){

                    boolean isDone;
                    if(taskCursor.getInt(taskCursor.getColumnIndex(TaskEntry.COLUMN_STATUS)) == 1)
                        isDone = true;
                    else
                        isDone = false;

                    Task task = new Task(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry._ID)) ,
                            mDbHelper.getAreaByID(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_AREA_ID))),
                            taskCursor.getString(taskCursor.getColumnIndex(TaskEntry.COLUMN_NAME)),
                            taskCursor.getString(taskCursor.getColumnIndex(TaskEntry.COLUMN_DESCRIPTION)),
                            taskDate,
                            isDone);

                    d.getTasks().add(task);

                    break;
                }
            }
        }
    }

    public void saveEditedTask(Task taskBeforeEdit, Task taskAfterEdit){

        Day previousDay = null;

        for(int i = 0; i < getItemCount(); i++){
            if(mDays.get(i).getDate().compareTo(taskBeforeEdit.getDate()) == 0) {
                previousDay = mDays.get(i);
                break;
            }
        }

        if(previousDay == null)
            return;

        int removeIndex = -1;

        for(int i = 0; i < previousDay.getTasks().size(); i++){
            if (previousDay.getTasks().get(i).equals(taskBeforeEdit)){
                if(taskBeforeEdit.getDate().compareTo(taskAfterEdit.getDate()) == 0){
                    previousDay.getTasks().get(i).setArea(taskAfterEdit.getArea());
                    previousDay.getTasks().get(i).setName(taskAfterEdit.getName());
                    previousDay.getTasks().get(i).setDescription(taskAfterEdit.getDescription());
                    previousDay.getTasks().get(i).setDone(taskAfterEdit.isDone());

                    notifyDataSetChanged();
                    return;
                } else {
                    removeIndex = i;
                    break;
                }
            }
        }

        if(removeIndex >= 0) {
            previousDay.getTasks().remove(removeIndex);
        }
        else
            return;

        Day newDay = null;

        for(int i = 0; i < getItemCount(); i++){
            if(mDays.get(i).getDate().compareTo(taskAfterEdit.getDate()) == 0) {
                newDay = mDays.get(i);
                break;
            }
        }

        if(newDay == null)
            return;

        newDay.addTask(taskAfterEdit);

        notifyDataSetChanged();


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

                viewHolder.taskShortDescriptionContainer.setBackgroundColor(Color.GRAY);
                viewHolder.addTaskButton.setBackgroundColor(Color.GRAY);



                view.setTag(viewHolder);

                taskContainers.add(view);
                container.addView(view);
            }
        }

        void bind(Day day){

            for(View v : taskContainers){
                DataTaskViewHolder viewHolder = (DataTaskViewHolder) v.getTag();
                final Area area = viewHolder.area;
                final Calendar today = Calendar.getInstance();
                today.set(Calendar.HOUR_OF_DAY, 0);
                today.set(Calendar.MINUTE, 0);
                today.set(Calendar.SECOND, 0);
                today.set(Calendar.MILLISECOND, 0);
                if(today.compareTo(day.getDate()) == 0)
                {
                    v.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                    viewHolder.taskShortDescriptionContainer.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                    viewHolder.addTaskButton.setBackgroundColor(mActivity.getResources().getColor(R.color.colorAccent));
                }else {
                    v.setBackgroundColor(mActivity.getResources().getColor(R.color.colorPrimaryLight));
                    viewHolder.taskShortDescriptionContainer.setBackgroundColor(Color.GRAY);
                    viewHolder.addTaskButton.setBackgroundColor(Color.GRAY);
                }
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

                        final Task task = t;
                        final DataTaskViewHolder dataTaskViewHolder = viewHolder;

                        viewHolder.taskShortDescriptionContainer.setClickable(true);
                        viewHolder.taskShortDescriptionContainer.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Snackbar snackbar = Snackbar.make(view, task.getName(), Snackbar.LENGTH_SHORT);
                                if(task.isDone()) {
                                    snackbar.setAction("MARK AS UNDONE", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            task.setDone(false);
                                            mDbHelper.updateTask(task);
                                            dataTaskViewHolder.taskStatus.setImageResource(R.drawable.checkbox_blank_circle_outline);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.RED);
                                }
                                else {
                                    snackbar.setAction("MARK AS DONE", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            task.setDone(true);
                                            mDbHelper.updateTask(task);
                                            dataTaskViewHolder.taskStatus.setImageResource(R.drawable.checkbox_marked_circle_outline);
                                        }
                                    });
                                    snackbar.setActionTextColor(Color.GREEN);
                                }

                                snackbar.show();
                            }
                        });
                        viewHolder.taskShortDescriptionContainer.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {

                                String status;
                                if(task.isDone())
                                    status = "Status: Done";
                                else
                                    status = "Status: Undone";
                                AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                                builder.setMessage(task.getDescription() + "\n\n" + status)
                                        .setTitle(task.getName())
                                        .setNegativeButton("Edit", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
                                                EditTaskDialogFragment newFragment = new EditTaskDialogFragment();
                                                newFragment.setTask(task);
                                                newFragment.setTaskDate(calendarDate);
                                                newFragment.setDataAdapter(DataAdapter.this);
                                                // The device is smaller, so show the fragment fullscreen
                                                FragmentTransaction transaction = fragmentManager.beginTransaction();
                                                // For a little polish, specify a transition animation
                                                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                                                // To make it fullscreen, use the 'content' root view as the container
                                                // for the fragment, which is always the root view for the activity
                                                transaction.add(android.R.id.content, newFragment)
                                                        .addToBackStack(null).commit();
                                            }
                                        })
                                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                dialogInterface.dismiss();
                                            }
                                        });

                                builder.show();
                                return false;
                            }
                        });

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