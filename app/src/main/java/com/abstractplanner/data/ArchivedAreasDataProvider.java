package com.abstractplanner.data;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Task;
import com.abstractplanner.utils.DateTimeUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import org.joda.time.Interval;
import org.joda.time.Period;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class ArchivedAreasDataProvider extends AbstractDataProvider {

    private static final String LOG_TAG = "ArchivedAreasDataProvid";

    public static final String PROVIDER_ID = "archived areas data provider";

    private List<AreaData> mData;
    private Context mContext;
    private AreaData mLastRemovedData;
    private AbstractPlannerDatabaseHelper mDbHelper;
    private int mLastRemovedPosition = -1;

    public ArchivedAreasDataProvider(AbstractPlannerDatabaseHelper dbHelper, Context context) {
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

        Cursor archivedAreasCursor = mDbHelper.getArchivedAreas();

        if(archivedAreasCursor.getCount() > 0){
            for(int i = 0; i < archivedAreasCursor.getCount(); i++) {
                archivedAreasCursor.moveToPosition(i);

                final long id = mData.size();
                final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                mData.add(new AreaData(id,
                        swipeReaction,
                        new Area(archivedAreasCursor.getLong(archivedAreasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry._ID)),
                                archivedAreasCursor.getString(archivedAreasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_NAME)),
                                archivedAreasCursor.getString(archivedAreasCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_DESCRIPTION)))));
            }
        }
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public AreaData getItem(int index) {
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

        final AreaData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void updateItem(int position){
        Area area = mData.get(position).getDataObject();

        if(area == null)
            return;

        long status = mDbHelper.updateArea(area);

        if(status < 0)
            return;

        if(!area.isArchived()) {
            Cursor areaTasksCursor = mDbHelper.getAllAreaTasks(mData.get(position).getDataObject().getId());

            if(areaTasksCursor != null && areaTasksCursor.getCount() > 0) {
                areaTasksCursor.moveToLast();

                Calendar yesterday = DateTimeUtils.getTodayDate();
                yesterday.add(Calendar.DATE, -1);

                Task latestTask = mDbHelper.getTaskByID(areaTasksCursor.getLong(areaTasksCursor.getColumnIndex(AbstractPlannerContract.TaskEntry._ID)));

                Period period = new Period(latestTask.getDate().getTimeInMillis(), yesterday.getTimeInMillis());

                if(latestTask.getDate().compareTo(yesterday) < 0){
                    for(int i = areaTasksCursor.getCount() - 1; i >= 0; i--){
                        areaTasksCursor.moveToPosition(i);
                        Task task = mDbHelper.getTaskByID(areaTasksCursor.getLong(areaTasksCursor.getColumnIndex(AbstractPlannerContract.TaskEntry._ID)));
                        task.getDate().add(Calendar.DATE, period.getDays());
                        mDbHelper.updateTask(task);
                    }
                }
            }

            mData.remove(position);
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

        final AreaData removedItem = mData.remove(position);

        mDbHelper.deleteArea(removedItem.getDataObject().getId());

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void refreshData() {
        mData.clear();
        loadData();
    }

    public static final class AreaData extends Data {

        private static final int ITEM_NORMAL = 0;

        private long mId;
        private Area mArea;
        private final String mText;
        private boolean mPinned;

        AreaData(long id, int swipeReaction, Area area) {
            mId = id;
            mArea = area;
            mText = makeText(area.getId(), area.getName(), swipeReaction);
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
            return ITEM_NORMAL;
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

        @Override
        public Area getDataObject() {
            return mArea;
        }

        @Override
        public void updateDataObject(Object objectData) {
            mArea = (Area) objectData;
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