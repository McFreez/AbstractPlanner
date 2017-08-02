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
import android.support.v7.app.AlertDialog;
import android.text.format.DateUtils;

import com.abstractplanner.data.AbstractPlannerContract.*;

import com.abstractplanner.dto.Task;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;

public class TasksDataProvider extends AbstractDataProvider {

    public static final String PROVIDER_ID = "today tasks data provider";

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

        Cursor tasksCursor = mDbHelper.getTodayTasks();

        if(tasksCursor.getCount() > 0){

            for(int i = 0; i < tasksCursor.getCount(); i++){
                tasksCursor.moveToPosition(i);

                if(i == 0){
                    insertHeader(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)), mContext, -1);
                } else {
                    long currentTaskTime = tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE));
                    tasksCursor.moveToPosition(i - 1);
                    long previousTaskTime = tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE));

                    tasksCursor.moveToPosition(i);

                    if(currentTaskTime != previousTaskTime){
                        insertHeader(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)), mContext, -1);
                    }
                }

                tasksCursor.moveToPosition(i);

                final long id = mData.size();
                final int viewType = TaskData.ITEM_NORMAL;
                final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                Calendar taskDate = Calendar.getInstance();
                taskDate.setTimeInMillis(tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry.COLUMN_DATE)));

                boolean isDone;
                if(tasksCursor.getInt(tasksCursor.getColumnIndex(TaskEntry.COLUMN_STATUS)) == 1)
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
                                isDone)));
            }
        }
    }

    private void insertHeader(final long taskDateTimeInMillis, Context context, int insertIndex){

        final long id = mData.size();
        final int viewType = TaskData.ITEM_HEADER;
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

        String day;

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        Calendar taskDate = Calendar.getInstance();
        taskDate.setTimeInMillis(taskDateTimeInMillis);

        if(today.compareTo(taskDate) == 0){
            day = "Today";
        } else {
            today.add(Calendar.DATE, - 1);
            if(today.compareTo(taskDate) == 0){
                day = "Yesterday";
            } else {
                Calendar previousYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) - 1, Calendar.DECEMBER, 31);
                Calendar nextYear = new GregorianCalendar(Calendar.getInstance().get(Calendar.YEAR) + 1, Calendar.JANUARY, 1);

                if(taskDate.after(previousYear) && taskDate.before(nextYear))
                    day = DateUtils.formatDateTime(context, taskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE);
                else
                    day = DateUtils.formatDateTime(context, taskDate.getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
            }
        }
        if(insertIndex < 0)
            mData.add(new TaskData(id, viewType, swipeReaction, day, taskDate));
        else
            mData.add(insertIndex, new TaskData(id, viewType, swipeReaction, day, taskDate));
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
        long id = mDbHelper.updateTask(mData.get(position).getDataObject());

        if(id < 0){
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setMessage("You already have task for "
                    + DateUtils.formatDateTime(mContext, mData.get(position).getDataObject().getDate().getTimeInMillis(), DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR)
                    + " on " + mData.get(position).getDataObject().getArea().getName() + ".")
                    .setTitle("Try another day or area")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    });

            builder.show();

            mData.get(position).setPinned(false);
        } else
            updateItemPosition(position);

    }

    private void updateItemPosition(int position){

        //final TaskData removedItem = mData.remove(position);
        removeItem(position, false);

        mLastRemovedData.setPinned(false);

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

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
            insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mContext, 0);
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
                insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mContext, insertPosition);
        } else {
            if (insertWithHeader)
                insertHeader(mLastRemovedData.getDate().getTimeInMillis(), mContext, -1);

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

        boolean isLastInDate = false;

        if(position != 0 && mData.get(position - 1).getViewType() == TaskData.ITEM_HEADER) {
            if (position == mData.size() - 1 || mData.get(position + 1).getViewType() == TaskData.ITEM_HEADER) {
                isLastInDate = true;
            }
        }

        //noinspection UnnecessaryLocalVariable
        final TaskData removedItem = mData.remove(position);

        if(setDone) {
            Task doneTask = removedItem.getDataObject();
            doneTask.setDone(true);

            mDbHelper.updateTask(doneTask);
        }

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

        private final long mId;
        private Task mTask;
        private final String mText;
        private Calendar mDate;
        private final int mViewType;
        private boolean mPinned;

        TaskData(long id, int viewType, int swipeReaction, Task task) {
            mDate = task.getDate();
            mId = id;
            mViewType = viewType;
            mTask = task;
            mText = makeText(task.getId(), task.getName(), swipeReaction);
        }

        TaskData(long id, int viewType, int swipeReaction, String day, Calendar date) {
            mId = id;
            mViewType = viewType;
            mTask = null;
            mText = day;
            mDate = date;
        }

        private static String makeText(long id, String text, int swipeReaction) {
            final StringBuilder sb = new StringBuilder();

            sb.append(id);
            sb.append(" - ");
            sb.append(text);

            return sb.toString();
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
