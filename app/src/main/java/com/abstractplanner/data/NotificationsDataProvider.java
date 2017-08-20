package com.abstractplanner.data;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.preference.PreferenceManager;

import com.abstractplanner.R;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.receivers.AlarmReceiver;
import com.abstractplanner.utils.DateTimeUtils;
import com.h6ah4i.android.widget.advrecyclerview.swipeable.RecyclerViewSwipeManager;

import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;

public class NotificationsDataProvider extends AbstractDataProvider {

    public static final String PROVIDER_ID = "notifications data provider";

    private List<NotificationData> mData;
    private Context mContext;
    private NotificationData mLastRemovedData;
    private AbstractPlannerDatabaseHelper mDbHelper;
    private int mLastRemovedPosition = -1;

    public NotificationsDataProvider(AbstractPlannerDatabaseHelper dbHelper, Context context) {
        mContext = context;
        mDbHelper = dbHelper;
        mData = new LinkedList<>();

        loadData();
    }

    private void loadData(){
        mData.clear();

        Cursor notificationsCursor = mDbHelper.getAllNotifications();

        if(notificationsCursor.getCount() > 0){

            boolean isEveryDayHeaderInserted = false;

            for(int i = 0; i < notificationsCursor.getCount(); i++){
                notificationsCursor.moveToPosition(i);

                if(notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)) ==
                    Notification.getNotificationTypeID(Notification.TYPE_SYSTEM_NAME)){

                    String message = notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_MESSAGE));

                    SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                    boolean isNotificationEnabled = false;

                    if(message.equals(mContext.getString(R.string.tomorrow_tasks_notification_message))){
                        isNotificationEnabled = sharedPreferences.getBoolean(mContext.getString(R.string.tomorrow_tasks_notification_key), true);
                    }

                    if(!isNotificationEnabled)
                        continue;

                    if(!isEveryDayHeaderInserted){
                        insertHeader("Every day", false);
                        isEveryDayHeaderInserted = true;
                    }

                    final long id = mData.size();
                    final int viewType = NotificationData.ITEM_STATIC;
                    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                    Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_DATE)),
                            TimeZone.getTimeZone(notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TIME_ZONE))));

                    long taskID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TASK_ID));
                    Task task = mDbHelper.getTaskByID(taskID);

                    mData.add(new NotificationData(id,
                            viewType,
                            swipeReaction,
                            new Notification(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry._ID)),
                                    message,
                                    notificationDate,
                                    task,
                                    notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)))));

                }
            }

            for(int i = 0; i < notificationsCursor.getCount(); i++){
                notificationsCursor.moveToPosition(i);

                if(notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)) ==
                        Notification.getNotificationTypeID(Notification.TYPE_EVERY_DAY_NAME)){

                    if(!isEveryDayHeaderInserted){
                        insertHeader("Every day", false);
                        isEveryDayHeaderInserted = true;
                    }

                    final long id = mData.size();
                    final int viewType = NotificationData.ITEM_NORMAL;
                    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                    Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_DATE)),
                            TimeZone.getTimeZone(notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TIME_ZONE))));

                    long taskID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TASK_ID));
                    Task task = mDbHelper.getTaskByID(taskID);

                    mData.add(new NotificationData(id,
                            viewType,
                            swipeReaction,
                            new Notification(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry._ID)),
                                    notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_MESSAGE)),
                                    notificationDate,
                                    task,
                                    notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)))));

                }
            }

            boolean isOneTimeHeaderInserted = false;

            for(int i = 0; i < notificationsCursor.getCount(); i++){
                notificationsCursor.moveToPosition(i);
                if(notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)) ==
                        Notification.getNotificationTypeID(Notification.TYPE_ONE_TIME_NAME)){
                    if(!isOneTimeHeaderInserted){
                        insertHeader("One time", false);
                        isOneTimeHeaderInserted = true;
                    }

                    final long id = mData.size();
                    final int viewType = NotificationData.ITEM_NORMAL;
                    final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

                    Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_DATE)),
                            TimeZone.getTimeZone(notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TIME_ZONE))));

                    long taskID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TASK_ID));
                    Task task = mDbHelper.getTaskByID(taskID);

                    mData.add(new NotificationData(id,
                            viewType,
                            swipeReaction,
                            new Notification(notificationsCursor.getLong(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry._ID)),
                                    notificationsCursor.getString(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_MESSAGE)),
                                    notificationDate,
                                    task,
                                    notificationsCursor.getInt(notificationsCursor.getColumnIndex(AbstractPlannerContract.NotificationEntry.COLUMN_TYPE)))));
                }
            }
        }
    }

    private void insertHeader(final String title, boolean insertAtStart){

        final long id = mData.size();
        final int viewType = NotificationData.ITEM_HEADER;
        final int swipeReaction = RecyclerViewSwipeManager.REACTION_CAN_SWIPE_UP | RecyclerViewSwipeManager.REACTION_CAN_SWIPE_DOWN;

        if(insertAtStart)
            mData.add(0, new NotificationData(id, viewType, swipeReaction, title));
        else
            mData.add(new NotificationData(id, viewType, swipeReaction, title));
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public NotificationsDataProvider.NotificationData getItem(int index) {
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

        final NotificationsDataProvider.NotificationData item = mData.remove(fromPosition);

        mData.add(toPosition, item);
        mLastRemovedPosition = -1;
    }

    @Override
    public void updateItem(int position){
        removeItem(position, false);

        switch (mLastRemovedData.getDataObject().getType()){
            case Notification.TYPE_ONE_TIME_ID:
                insertIntoOneTimeNotification();
                break;
            case Notification.TYPE_EVERY_DAY_ID:
                insertIntoEveryDayNotifications();
                break;
        }

        mLastRemovedData = null;
    }

    private void insertIntoEveryDayNotifications(){

        if (mData.size() == 0){
            insertHeader(Notification.TYPE_EVERY_DAY_NAME, false);
            mData.add(mLastRemovedData);
            return;
        }

        if(mData.get(0).getText().equals(Notification.TYPE_EVERY_DAY_NAME)){
            mData.add(1, mLastRemovedData);
        }
        else{
            insertHeader(Notification.TYPE_EVERY_DAY_NAME, true);
            mData.add(1, mLastRemovedData);
        }
    }

    private void insertIntoOneTimeNotification(){

        if(mData.size() == 0){
            insertHeader(Notification.TYPE_ONE_TIME_NAME, false);
            mData.add(mLastRemovedData);
            return;
        }

        int headerItemIndex = -1;

        for (int i = 0; i < mData.size(); i++){
            if(mData.get(i).getText().equals(Notification.TYPE_ONE_TIME_NAME)){
                headerItemIndex = i;
                break;
            }
        }

        if(headerItemIndex >= 0){
            mData.add(headerItemIndex + 1, mLastRemovedData);
        }
        else{
            insertHeader(Notification.TYPE_ONE_TIME_NAME, false);
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
    public void removeItem(int position, boolean delete) {

        boolean isLastInCathegory = false;

        if(position != 0 && mData.get(position - 1).getViewType() == NotificationData.ITEM_HEADER) {
            if (position == mData.size() - 1 || mData.get(position + 1).getViewType() == NotificationData.ITEM_HEADER) {
                isLastInCathegory = true;
            }
        }

        //noinspection UnnecessaryLocalVariable
        final NotificationData removedItem = mData.remove(position);

        if(delete) {
            Notification removedNotification = removedItem.getDataObject();

            Intent alarmIntent = new Intent(mContext, AlarmReceiver.class);
            alarmIntent.putExtra("message", removedNotification.getMessage());
            if(removedNotification.getTask() != null)
                alarmIntent.putExtra("title", removedNotification.getTask().getArea().getName());
            else
                alarmIntent.putExtra("title", Notification.getNotificationTypeName(removedNotification.getType()) + " notification");
            alarmIntent.putExtra("id", removedNotification.getId());

            Long idLong = removedNotification.getId();
            int id = idLong.intValue();

            PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, id, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager manager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
            manager.cancel(pendingIntent);

            mDbHelper.deleteNotification(removedNotification.getId());
        }

        if(isLastInCathegory)
            mData.remove(position - 1);

        mLastRemovedData = removedItem;
        mLastRemovedPosition = position;
    }

    @Override
    public void refreshData() {
        mData.clear();
        loadData();
    }

    public static final class NotificationData extends Data {

        public static final int ITEM_NORMAL = 0;
        public static final int ITEM_HEADER = 1;
        public static final int ITEM_STATIC = 2;

        private final long mId;
        private Notification mNotification;
        private final String mText;
        private final int mViewType;
        private boolean mPinned;

        NotificationData(long id, int viewType, int swipeReaction, Notification notification) {
            mId = id;
            mViewType = viewType;
            mNotification = notification;
            mText = makeText(notification.getId(), notification.getMessage(), swipeReaction);
        }

        NotificationData(long id, int viewType, int swipeReaction, String title) {
            mId = id;
            mViewType = viewType;
            mNotification = null;
            mText = title;
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

        @Override
        public Notification getDataObject() {
            return mNotification;
        }

        @Override
        public void updateDataObject(Object objectData) {
            mNotification = (Notification) objectData;
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
