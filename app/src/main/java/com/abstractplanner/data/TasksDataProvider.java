/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.abstractplanner.data;

import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;
import android.util.Log;

import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerContract.*;

import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Task;
import com.abstractplanner.utils.DateTimeUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class TasksDataProvider extends AbstractDataProvider {

    private static final String LOG_TAG = "TasksDataProvider";

    public static final String PROVIDER_ID = "today tasks data provider";

    private static final String QUICK_TASKS_HEDAER = "Quick tasks";

    private List<TaskData> mData;
    private Context mContext;
    private TaskData mLastRemovedData;
    private AbstractPlannerDatabaseHelper mDbHelper;
    private int mLastRemovedPosition = -1;

    public TasksDataProvider(AbstractPlannerDatabaseHelper dbHelper, Context context) {
/*
        final String atoz = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
*/
        mContext = context;
        mDbHelper = dbHelper;
        mData = new LinkedList<>();

        loadData();

/*        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < atoz.length(); j++) {
                final long id = mData.size();
                final int viewType = 0;
                final String text = Character.toString(atoz.charAt(j));
                final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;
                mData.add(new TaskData(id, viewType, text, swipeReaction));
            }
        }*/
    }

    private void loadData(){
        mData.clear();

        Cursor quickTasksCursor = mDbHelper.getQuickTasks();

        if(quickTasksCursor.getCount() > 0){
            for(int i = 0; i < quickTasksCursor.getCount(); i++) {
                quickTasksCursor.moveToPosition(i);

                int type = quickTasksCursor.getInt(quickTasksCursor.getColumnIndex(TaskEntry.COLUMN_TYPE));

                if (mData.size() == 0) {
                    insertHeader(0, null, mContext, -1, type);
                }

                final long id = mData.size();
                final int viewType = TaskData.ITEM_NORMAL;
                final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                Calendar taskDate = null;

                boolean isDone = false;

                Area area = null;

                mData.add(new TaskData(id,
                        viewType,
                        swipeReaction,
                        new Task(quickTasksCursor.getLong(quickTasksCursor.getColumnIndex(TaskEntry._ID)),
                                area,
                                quickTasksCursor.getString(quickTasksCursor.getColumnIndex(TaskEntry.COLUMN_NAME)),
                                quickTasksCursor.getString(quickTasksCursor.getColumnIndex(TaskEntry.COLUMN_DESCRIPTION)),
                                taskDate,
                                isDone,
                                type)));
            }
        }

        Cursor tasksCursor = mDbHelper.getTodayTasks();

        if(tasksCursor.getCount() > 0){

            boolean isFirstHeaderCreated = false;

            for(int i = 0; i < tasksCursor.getCount(); i++) {
                tasksCursor.moveToPosition(i);

                int type = tasksCursor.getInt(tasksCursor.getColumnIndex(TaskEntry.COLUMN_TYPE));

                if (!isFirstHeaderCreated) {
                    insertHeader(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)),
                            TimeZone.getTimeZone(tasksCursor.getString(tasksCursor.getColumnIndex(TaskEntry.COLUMN_TIME_ZONE))), mContext, -1, type);
                    isFirstHeaderCreated = true;
                } else {
                    long currentTaskTime = tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE));
                    tasksCursor.moveToPosition(i - 1);
                    long previousTaskTime = tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE));

                    tasksCursor.moveToPosition(i);

                    if (currentTaskTime != previousTaskTime) {
                        insertHeader(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)),
                                TimeZone.getTimeZone(tasksCursor.getString(tasksCursor.getColumnIndex(TaskEntry.COLUMN_TIME_ZONE))), mContext, -1, type);
                    }
                }

                tasksCursor.moveToPosition(i);

                final long id = mData.size();
                final int viewType = TaskData.ITEM_NORMAL;
                final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                Calendar taskDate = DateTimeUtils.getInstanceDayInCurrentTimeZone(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)),
                        TimeZone.getTimeZone(tasksCursor.getString(tasksCursor.getColumnIndex(TaskEntry.COLUMN_TIME_ZONE))));

                boolean isDone;
                if (tasksCursor.getInt(tasksCursor.getColumnIndex(TaskEntry.COLUMN_STATUS)) == 1)
                    isDone = true;
                else
                    isDone = false;

                mData.add(new TaskData(id,
                        viewType,
                        swipeReaction,
                        new Task(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry._ID)),
                                mDbHelper.getAreaByID(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_AREA_ID))),
                                tasksCursor.getString(tasksCursor.getColumnIndex(TaskEntry.COLUMN_NAME)),
                                tasksCursor.getString(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DESCRIPTION)),
                                taskDate,
                                isDone,
                                type)));
            }
        }

        setColors();
    }

    private void insertHeader(final long taskDateTimeInMillis, TimeZone timeZone, Context context, int insertIndex, int taskType){

        long id = mData.size();
        final int viewType = TaskData.ITEM_HEADER;
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

        String day;
        Calendar taskDate = null;

        if(taskType == Task.TYPE_QUICK){
            day = QUICK_TASKS_HEDAER;
        } else {
            Calendar today = DateTimeUtils.getTodayDate();

            taskDate = DateTimeUtils.getInstanceDayInCurrentTimeZone(taskDateTimeInMillis, timeZone);

            if (today.compareTo(taskDate) == 0) {
                day = "Today";
            } else {
                today.add(Calendar.DATE, -1);
                if (today.compareTo(taskDate) == 0) {
                    day = "Yesterday";
                } else {
                    Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                    Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                    if (taskDate.after(previousYear) && taskDate.before(nextYear))
                        day = DateUtils.formatDateTime(context, taskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                    else
                        day = DateUtils.formatDateTime(context, taskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                }
            }
        }
        if(insertIndex < 0)
            mData.add(new TaskData(id, viewType, swipeReaction, day, taskDate));
        else {
            id = insertIndex;
            mData.add(insertIndex, new TaskData(id, viewType, swipeReaction, day, taskDate));
            if(insertIndex + 1 < mData.size()) {
                for (int i = insertIndex + 1; i < mData.size(); i++) {
                    mData.get(i).setId(mData.get(i).getId() + 1);
                }
            }
        }
    }

    private void setColors(){

        if(mData.size() == 0)
            return;

        int headersCount = 0;

        for(int i = 0; i < mData.size(); i++)
            if(mData.get(i).getViewType() == TaskData.ITEM_HEADER && !mData.get(i).getText().equals(QUICK_TASKS_HEDAER))
                headersCount++;

        if(headersCount <= 6){
            setColors6Headers();
            return;
        }

        setColorsMoreThan6Headers(headersCount);
    }

    private void setColors6Headers(){

        int colorIndex = 0;

        int[] taskColors = {
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_6, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_5, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_4, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_3, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_2, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_1, null),
        };

        for(int i = mData.size() - 1; i >= 0; i--){
            if(mData.get(i).getViewType() == TaskData.ITEM_HEADER){
                if(!mData.get(i).getText().equals(QUICK_TASKS_HEDAER)) {
                    colorIndex++;

                    if (colorIndex >= taskColors.length)
                        break;
                }

                continue;
            }

            if(mData.get(i).getDataObject().getType() != Task.TYPE_QUICK)
/*                mData.get(i).setStatusColor(ResourcesCompat.getColor(mContext.getResources(), android.R.color.transparent, null));
            else*/
                mData.get(i).setStatusColor(taskColors[colorIndex]);
        }
    }

    private void setColorsMoreThan6Headers(final int headersCount){

        int headersLeft = headersCount;
        int firstItemIndex = -1;

        for(int i = 0; i < mData.size(); i++){
            if(mData.get(i).getViewType() == TaskData.ITEM_NORMAL && mData.get(i).getDataObject().getType() == Task.TYPE_NORMAL) {
                firstItemIndex = i;
                break;
            }
        }

        if(firstItemIndex < 0)
            return;

        long latestDateMillis = mData.get(firstItemIndex).getDataObject().getDate().getTimeInMillis();

        long earliestDateMillis = mData.get(mData.size() - 1).getDataObject().getDate().getTimeInMillis();

        long diff = earliestDateMillis - latestDateMillis;

        int colorIndex = 0;

        int[] taskColors = {
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_6, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_5, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_4, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_3, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_2, null),
                ResourcesCompat.getColor(mContext.getResources(), R.color.status_red_1, null),
        };

        long step = diff / taskColors.length - 1;

        Calendar taskDateStep1 = Calendar.getInstance();
        taskDateStep1.setTimeInMillis(earliestDateMillis);

        Calendar taskDateStep2 = Calendar.getInstance();
        taskDateStep2.setTimeInMillis(earliestDateMillis - step);

        Calendar taskDateStep3 = Calendar.getInstance();
        taskDateStep3.setTimeInMillis(earliestDateMillis - step * 2);

        Calendar taskDateStep4 = Calendar.getInstance();
        taskDateStep4.setTimeInMillis(earliestDateMillis - step * 3);

        Calendar taskDateStep5 = Calendar.getInstance();
        taskDateStep5.setTimeInMillis(earliestDateMillis - step * 4);

        Calendar taskDateStep6 = Calendar.getInstance();
        taskDateStep6.setTimeInMillis(latestDateMillis);

        int taskDateIndex = 0;

        Calendar[] taskDateSteps = {
                taskDateStep1,
                taskDateStep2,
                taskDateStep3,
                taskDateStep4,
                taskDateStep5,
                taskDateStep6
        };

        boolean everyHeaderChange = false;

        for(int i = mData.size() - 1; i >= firstItemIndex; i--){
            if(mData.get(i).getViewType() == TaskData.ITEM_HEADER){
                if(i == 0)
                    break;

                if(everyHeaderChange){
                    colorIndex++;
                    //Log.e("TaskDataProvider", "every header change index : " + colorIndex + " i : " + i + " headers left " + headersLeft);
                    continue;
                }

                headersLeft--;

                if(headersLeft == taskColors.length - colorIndex - 1){
                    everyHeaderChange = true;
                    colorIndex++;
                    //Log.e("TaskDataProvider", "headers left and colors comparing index : " + colorIndex + " i : " + i + " headers left " + headersLeft);
                    continue;
                }

/*                Log.e("TaskDataProvider", "first comparing : " + mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex])
                        + " second comparing + 1 : " + mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 1]));*/

                if(mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex]) <= 0
                        && mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 1]) >= 0){
                    //Log.e("TaskDataProvider", " Stay on color : " + colorIndex);

                    continue;
                } else {
                    if(taskDateSteps.length <= taskDateIndex + 2)
                        continue;

/*                    Log.e("TaskDataProvider", "first comparing + 1 : " + mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 1])
                            + " second comparing + 2 : " + mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 2]));*/

                    if (mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 1]) <= 0
                            && mData.get(i - 1).getDataObject().getDate().compareTo(taskDateSteps[taskDateIndex + 2]) >= 0) {
                        taskDateIndex++;
                        colorIndex++;

                        //Log.e("TaskDataProvider", " + 1 + 2 : " + colorIndex);

                        continue;
                    } else {
                        long earliestDateTiM = mData.get(i - 1).getDataObject().getDate().getTimeInMillis();

                        long diffTiM = earliestDateTiM - latestDateMillis;

                        for (int j = taskDateSteps.length - 2; j > taskDateIndex; j--) {
                            taskDateSteps[j].setTimeInMillis(taskDateSteps[j + 1].getTimeInMillis() + diffTiM);
                        }

                        taskDateSteps[taskDateIndex + 1].setTimeInMillis(earliestDateTiM);

                        taskDateIndex++;
                        colorIndex++;

                        //Log.e("Tas kDataProvider", " recalculate date : " + colorIndex);

                        continue;
                    }
                }
            }

            mData.get(i).setStatusColor(taskColors[colorIndex]);
            //Log.e("TaskDataProvider", " SET COLOR " + colorIndex);
        }

/*        if(firstItemIndex > 1){
            for(int i = firstItemIndex; i >= 0; i--){
                if(mData.get(i).getViewType() == TaskData.ITEM_NORMAL){
                    mData.get(i).setStatusColor(ResourcesCompat.getColor(mContext.getResources(), android.R.color.transparent, null));
                }
            }
        }*/
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public TaskData getItem(int index) {
        if (index < 0 || index >= getCount()) {
            throw new IndexOutOfBoundsException("index = " + index);
        }

        return mData.get(index);
    }

    @Override
    public int undoLastRemoval() {
        if (mLastRemovedData != null) {
            int insertedPosition;
            if (mLastRemovedPosition >= 0 && mLastRemovedPosition < mData.size()) {
                insertedPosition = mLastRemovedPosition;
            } else {
                insertedPosition = mData.size();
            }

            mData.add(insertedPosition, mLastRemovedData);

            mLastRemovedData = null;
            mLastRemovedPosition = -1;

            return insertedPosition;
        } else {
            return -1;
        }
    }

    public int addQuickTaskItem(Task task){
        if(task.getType() != Task.TYPE_QUICK)
            return -1;

        if(mData.size() == 0){
            insertHeader(0, null, mContext, -1, task.getType());

            final long id = mData.size();
            final int viewType = TaskData.ITEM_NORMAL;
            final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

            Calendar taskDate = null;

            boolean isDone = false;

            Area area = null;

            mData.add(new TaskData(id,
                    viewType,
                    swipeReaction,
                    task));

            //mData.get(mData.size() - 1).setStatusColor(ResourcesCompat.getColor(mContext.getResources(), android.R.color.transparent, null));

            return mData.size() - 1;
        } else {
            if (!mData.get(0).mText.equals(QUICK_TASKS_HEDAER))
                insertHeader(0, null, mContext, 0, task.getType());

            int insertIndex = 1;

            final long id = insertIndex;
            final int viewType = TaskData.ITEM_NORMAL;
            final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

            Calendar taskDate = null;

            boolean isDone = false;

            Area area = null;

            mData.add(insertIndex, new TaskData(id,
                    viewType,
                    swipeReaction,
                    task));

            if(insertIndex + 1 < mData.size()) {
                for (int i = insertIndex + 1; i < mData.size(); i++) {
                    mData.get(i).setId(mData.get(i).getId() + 1);
                }
            }
            //mData.get(insertIndex).setStatusColor(ResourcesCompat.getColor(mContext.getResources(), android.R.color.transparent, null));

            return insertIndex;
        }
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        final TaskData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void updateItem(int position){
/*        long id = mDbHelper.updateTask(mData.get(position).getDataObject());

        Task task = mData.get(position).getDataObject();

        if()
        if(id < 0) {
            if (id == -1) {
                AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                builder.setMessage("You already have task for "
                        + DateUtils.formatDateTime(mContext, task.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                        + " on " + task.getArea().getName() + ".")
                        .setTitle("Try another day or area")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });

                builder.show();
            } else if (id == -2) {
                long taskId = mDbHelper.isAllPreviousAreaTasksDone(task);
                if (taskId > 0) {
                    Task undoneTask = mDbHelper.getTaskByID(taskId);

                    Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                    Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                    String dateString;

                    if (undoneTask.getDate().after(previousYear) && undoneTask.getDate().before(nextYear))
                        dateString = DateUtils.formatDateTime(mContext, undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                    else
                        dateString = DateUtils.formatDateTime(mContext, undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setMessage("You have unfinished task for " + dateString + " in " + task.getArea().getName() + ". Finish it first, please.")
                            .setTitle("Finish earlier task first")
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });

                    builder.show();
                }
            }

            mData.get(position).setPinned(false);
        } else
            updateItemPosition(position);*/

        Task task = mDbHelper.getTaskByID(mData.get(position).getDataObject().getId());//mData.get(position).getDataObject();
        if(task == null)
            return;

        if(task.getType() == Task.TYPE_QUICK){
            mData.get(position).updateDataObject(task);
        } else
            updateItemPosition(position);

    }

    private void updateItemPosition(int position){

        //final TaskData removedItem = mData.remove(position);
        removeItem(position, false);

        mLastRemovedData.setPinned(false);

        Calendar today = DateTimeUtils.getTodayDate();

        if(today.compareTo(mLastRemovedData.getDate()) < 0)
            return;

        int insertPosition = -1;
        boolean dateFound = false;
        boolean insertWithHeader = false;
        boolean insertToEnd = false;

        if(mData.size() == 0) {
            loadData();
            return;
        }

        if(mData.get(0).getDate().compareTo(mLastRemovedData.getDate()) > 0){
            mData.add(0, mLastRemovedData);
            insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mLastRemovedData.getDate().getTimeZone(), mContext, 0, mLastRemovedData.getDataObject().getType());
            return;
        }

        for(int i = 0; i < mData.size(); i++){
            if(!dateFound){
                if(i + 1 == mData.size()){
                    insertWithHeader = true;
                    insertToEnd = true;

                    break;
                } else
                    if(mData.get(i).getViewType() == TaskData.ITEM_HEADER) {
                        if (mData.get(i).getDate().compareTo(mLastRemovedData.getDate()) == 0) {
                            dateFound = true;
                            continue;
                        } else if (mData.get(i).getDate().compareTo(mLastRemovedData.getDate()) > 0) {
                            insertWithHeader = true;
                            insertPosition = i;

                            break;
                        }
                    }
            }

            if(dateFound){
                if(i + 1 == mData.size()){
                    insertWithHeader = false;
                    insertToEnd = true;

                    break;
                } else
                    if(mData.get(i + 1).getViewType() == TaskData.ITEM_HEADER){
                        insertPosition = i + 1;
                        break;
                    }
            }
        }


        if(!insertToEnd && insertPosition < 0)
            return;

        if(!insertToEnd) {
            mData.add(insertPosition, mLastRemovedData);

            if (insertWithHeader)
                insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mLastRemovedData.getDate().getTimeZone(), mContext, insertPosition, mLastRemovedData.getDataObject().getType());
        } else {
            if (insertWithHeader)
                insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mLastRemovedData.getDate().getTimeZone(), mContext, -1, mLastRemovedData.getDataObject().getType());

            mData.add(mLastRemovedData);
        }
    }

    @Override
    public void swapItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }

        Collections.swap(mData, toPosition, fromPosition);
        mLastRemovedPosition = -1;
    }

    @Override
    public void removeItem(int position, boolean setDone) {

        if(setDone) {
            Task doneTask = mData.get(position).getDataObject();
            doneTask.setDone(true);

            if(doneTask.getType() == Task.TYPE_QUICK){
                mDbHelper.deleteTask(doneTask.getId());
            } else {
                long status = mDbHelper.updateTask(doneTask);
                if (status < 0) {
                    if (status == -1) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                        builder.setMessage("You already have task for "
                                + DateUtils.formatDateTime(mContext, doneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                                + " on " + doneTask.getArea().getName() + ".")
                                .setTitle("Try another day or area")
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        dialogInterface.dismiss();
                                    }
                                });

                        builder.show();
                        return;
                    } else if (status == -2) {
                        long taskId = mDbHelper.isAllPreviousAreaTasksDone(doneTask);
                        if (taskId > 0) {
                            Task undoneTask = mDbHelper.getTaskByID(taskId);

                            Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                            Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                            String dateString;

                            if (undoneTask.getDate().after(previousYear) && undoneTask.getDate().before(nextYear))
                                dateString = DateUtils.formatDateTime(mContext, undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                            else
                                dateString = DateUtils.formatDateTime(mContext, undoneTask.getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);

                            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                            builder.setMessage("You have unfinished task for " + dateString + " in " + doneTask.getArea().getName() + ". Finish it first, please.")
                                    .setTitle("Finish earlier task first")
                                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int i) {
                                            dialogInterface.dismiss();
                                        }
                                    });

                            builder.show();
                            return;
                        }
                    }
                }
            }
        }

        boolean isLastInDate = false;

        if(position != 0 && mData.get(position - 1).getViewType() == TaskData.ITEM_HEADER) {
            if (position == mData.size() - 1 || mData.get(position + 1).getViewType() == TaskData.ITEM_HEADER) {
                isLastInDate = true;
            }
        }

        //noinspection UnnecessaryLocalVariable
        final TaskData removedItem = mData.remove(position);

        if(isLastInDate)
            mData.remove(position - 1);

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void refreshData() {
        mData.clear();
        loadData();
    }

    public static final class TaskData extends Data {

        public static final int ITEM_NORMAL = 0;
        public static final int ITEM_HEADER = 1;

        private long mId;
        private Task mTask;
        private final String mText;
        private Calendar mDate;
        private final int mViewType;
        private boolean mPinned;
        private int mColor;

        TaskData(long id, int viewType, int swipeReaction, Task task) {
            mDate = task.getDate();
            mId = id;
            mViewType = viewType;
            mTask = task;
            mText = makeText(task.getId(), task.getName(), swipeReaction);
            mColor = 0;
        }

        TaskData(long id, int viewType, int swipeReaction, String day, Calendar date) {
            mId = id;
            mViewType = viewType;
            mTask = null;
            mText = day;
            mDate = date;
            mColor = 0;
        }

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();

            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return sb.toString();
        }

        public void setStatusColor(int color){
            mColor = color;
        }

        public int getStatusColor(){
            return mColor;
        }

        @Override
        public boolean isSectionHeader() {
            return false;
        }

        @Override
        public int getViewType() {
            return mViewType;
        }

        @Override
        public long getId() {
            return mId;
        }

        public void setId(long id){
            mId = id;
        }

        @Override
        public String toString() {
            return mText;
        }

        @Override
        public String getText() {
            return mText;
        }

        public Calendar getDate() {
            if(mViewType == ITEM_NORMAL){
                mDate = mTask.getDate();
            }
            return mDate;
        }

        @Override
        public Task getDataObject() {
            return mTask;
        }

        @Override
        public void updateDataObject(Object objectData) {
            mTask = (Task) objectData;
        }

        @Override
        public boolean isPinned() {
            return mPinned;
        }

        @Override
        public void setPinned(boolean pinned) {
            mPinned = pinned;
        }
    }
}
