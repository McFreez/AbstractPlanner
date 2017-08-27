package com.abstractplanner.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.abstractplanner.R;
import com.abstractplanner.data.AbstractPlannerContract.*;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Notification;
import com.abstractplanner.dto.Task;
import com.abstractplanner.utils.DateTimeUtils;
import com.abstractplanner.utils.NotificationUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

public class AbstractPlannerDatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_TAG = AbstractPlannerDatabaseHelper.class.getName();

    private static final int DATABASE_VERSION = 6;
    public static final String DATABASE_NAME = "abstractPlanner.db";

    private static final String CREATE_TABLE_TASK = "CREATE TABLE " + TaskEntry.TABLE_NAME + " ("
            + TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + TaskEntry.COLUMN_NAME + " TEXT NOT NULL,"
            + TaskEntry.COLUMN_DESCRIPTION + " TEXT,"
            + TaskEntry.COLUMN_AREA_ID + " INTEGER,"
            + TaskEntry.COLUMN_DATE + " INTEGER,"
            + TaskEntry.COLUMN_TIME_ZONE + " TEXT,"
            + TaskEntry.COLUMN_STATUS + " INTEGER NOT NULL,"
            + TaskEntry.COLUMN_TYPE + " INTEGER NOT NULL,"
            + " UNIQUE (" + TaskEntry.COLUMN_AREA_ID + "," + TaskEntry.COLUMN_DATE + "," + TaskEntry.COLUMN_TIME_ZONE + ") ON CONFLICT FAIL);";

    private Context mContext;

    private boolean mIsDbInitial;
    //private final Object mLock = new Object();

    public AbstractPlannerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mContext = context;

        mIsDbInitial = AbstractPlannerPreferences.isDatabaseInInitialStatus(mContext);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_AREA = "CREATE TABLE " + AreaEntry.TABLE_NAME + " ("
                + AreaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AreaEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + AreaEntry.COLUMN_DESCRIPTION + " TEXT,"
                + " UNIQUE (" + AreaEntry.COLUMN_NAME + ") ON CONFLICT FAIL);";

        final String CREATE_TABLE_NOTIFICATION = "CREATE TABLE " + NotificationEntry.TABLE_NAME + " ("
                + NotificationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NotificationEntry.COLUMN_MESSAGE + " TEXT NOT NULL,"
                + NotificationEntry.COLUMN_DATE + " INTEGER NOT NULL,"
                + NotificationEntry.COLUMN_TIME_ZONE + " TEXT NOT NULL,"
                + NotificationEntry.COLUMN_TASK_ID + " INTEGER,"
                + NotificationEntry.COLUMN_TYPE + " INTEGER NOT NULL" + ");";

        db.execSQL(CREATE_TABLE_AREA);
        db.execSQL(CREATE_TABLE_TASK);
        db.execSQL(CREATE_TABLE_NOTIFICATION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /*db.execSQL("DROP TABLE IF EXISTS " + NotificationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AreaEntry.TABLE_NAME);

        onCreate(db);*/

        db.beginTransaction();
        Log.i(LOG_TAG, "Transaction began");
        try {
            db.execSQL("ALTER TABLE " + TaskEntry.TABLE_NAME + " ADD COLUMN " + TaskEntry.COLUMN_TYPE + " INTEGER DEFAULT " + Task.TYPE_NORMAL);
            Log.i(LOG_TAG, "Column added");

            db.execSQL("ALTER TABLE " + TaskEntry.TABLE_NAME + " RENAME TO " + TaskEntry.TABLE_NAME + "_old");
            db.execSQL(CREATE_TABLE_TASK);
            db.execSQL("INSERT INTO " + TaskEntry.TABLE_NAME + " SELECT * FROM " + TaskEntry.TABLE_NAME + "_old");
            Log.i(LOG_TAG, "Old table replaced with new");

            db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME + "_old");
            Log.i(LOG_TAG, "Old table removed");

            db.setTransactionSuccessful();
            Log.i(LOG_TAG, "Transaction succeed");
        }
        finally {
            db.endTransaction();
            Log.i(LOG_TAG, "Transaction ended");
        }
    }

    // Areas

    public Cursor getAllAreas(){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                AreaEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public long createArea(Area area){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AreaEntry.COLUMN_NAME, area.getName());
        values.put(AreaEntry.COLUMN_DESCRIPTION, area.getDescription());

        try {
            long id = db.insert(AreaEntry.TABLE_NAME, null, values);

            setDbNotInitialStatus();

            return id;
        }catch (SQLiteConstraintException e){
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    public Area getAreaByID(long area_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor areaCursor = db.query(AreaEntry.TABLE_NAME,
                null,
                AreaEntry._ID + " = ?",
                new String[]{ String.valueOf(area_id) },
                null,
                null,
                null);

        if(areaCursor != null)
            areaCursor.moveToFirst();
        else
            return null;

        return new Area(areaCursor.getLong(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry._ID)),
                areaCursor.getString(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_NAME)),
                areaCursor.getString(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_DESCRIPTION)));
    }

    public Area getAreaByName(String areaName){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor areaCursor = db.query(AreaEntry.TABLE_NAME,
                null,
                AreaEntry.COLUMN_NAME + " = ?",
                new String[]{ areaName },
                null,
                null,
                null);

        if(areaCursor != null)
            areaCursor.moveToFirst();
        else
            return null;

        return new Area(areaCursor.getLong(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry._ID)),
                areaCursor.getString(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_NAME)),
                areaCursor.getString(areaCursor.getColumnIndex(AbstractPlannerContract.AreaEntry.COLUMN_DESCRIPTION)));
    }

    public long updateArea(Area area){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(AreaEntry.COLUMN_NAME, area.getName());
        values.put(AreaEntry.COLUMN_DESCRIPTION, area.getDescription());

        try{
            long id = db.update(AreaEntry.TABLE_NAME, values, AreaEntry._ID + " = ?",
                    new String[]{ String.valueOf(area.getId()) });

            setDbNotInitialStatus();

            return id;
        }
        catch (SQLiteConstraintException e){
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    public void deleteArea(long area_id){
        deleteAllAreaTasks(area_id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AreaEntry.TABLE_NAME, AreaEntry._ID + " = ?",
                new String[]{ String.valueOf(area_id) });

        setDbNotInitialStatus();
    }

    // Tasks

    public Cursor getTasksInRange(Calendar startDate, Calendar endDate){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_DATE + " >= ? AND " + TaskEntry.COLUMN_DATE + " <= ?",
                new String[]{ String.valueOf(startDate.getTimeInMillis()), String.valueOf(endDate.getTimeInMillis()) },
                null,
                null,
                TaskEntry.COLUMN_DATE);
    }

    public Cursor getTodayTasks(){
        //synchronized (mLock) {
            SQLiteDatabase db = this.getReadableDatabase();

            Calendar today = DateTimeUtils.getTodayDate();

            return db.query(TaskEntry.TABLE_NAME,
                    null,
                    TaskEntry.COLUMN_STATUS + " = ? AND " + TaskEntry.COLUMN_DATE + " <= ?",
                    new String[]{String.valueOf(0), String.valueOf(today.getTimeInMillis())},
                    null,
                    null,
                    TaskEntry.COLUMN_DATE + " ASC");
        //}
    }

    public Cursor getQuickTasks(){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_TYPE + " = ?",
                new String[]{String.valueOf(Task.TYPE_QUICK)},
                null,
                null,
                null);
    }

    public Cursor getAllAreaTasks(long area_id){
        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_AREA_ID + " = ?",
                new String[] { String.valueOf(area_id) },
                null,
                null,
                null);
    }

    // Returns count of undone tasks in area
    // including today and past days tasks
    public int getUndoneTasksInAreaCount(long area_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Calendar today = DateTimeUtils.getTodayDate();

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_AREA_ID + " = ? AND " + TaskEntry.COLUMN_DATE + " <= ? AND " + TaskEntry.COLUMN_STATUS + " = ?",
                new String[]{ String.valueOf(area_id), String.valueOf(today.getTimeInMillis()), String.valueOf(0) },
                null,
                null,
                null).getCount();
    }

    public Task getTaskByID(long task_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor taskCursor = db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry._ID + " = ?",
                new String[]{ String.valueOf(task_id) },
                null,
                null,
                null);

        if(taskCursor != null && taskCursor.getCount() > 0)
            taskCursor.moveToFirst();
        else
            return null;

        Calendar taskDate = null;

        if(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_DATE)) != 0) {
            taskDate = DateTimeUtils.getInstanceDayInCurrentTimeZone(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_DATE)),
                    TimeZone.getTimeZone(taskCursor.getString(taskCursor.getColumnIndex(TaskEntry.COLUMN_TIME_ZONE))));
        }

        Area area = null;

        if(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_AREA_ID)) != 0){
            area = getAreaByID(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry.COLUMN_AREA_ID)));
        }

        boolean isDone;
        if(taskCursor.getInt(taskCursor.getColumnIndex(TaskEntry.COLUMN_STATUS)) == 1)
            isDone = true;
        else
            isDone = false;

        return new Task(taskCursor.getLong(taskCursor.getColumnIndex(TaskEntry._ID)),
                area,
                taskCursor.getString(taskCursor.getColumnIndex(TaskEntry.COLUMN_NAME)),
                taskCursor.getString(taskCursor.getColumnIndex(TaskEntry.COLUMN_DESCRIPTION)),
                taskDate,
                isDone,
                taskCursor.getInt(taskCursor.getColumnIndex(TaskEntry.COLUMN_TYPE)));
    }

    public boolean isTasksForTomorrowSet(){
        Cursor allAreasCursor = getAllAreas();

        if(allAreasCursor == null || allAreasCursor.getCount() <= 0)
            return true;

        int areasCount = allAreasCursor.getCount();

        Cursor tomorrowTasksCursor = getAllTasksForTomorrow();

        if(tomorrowTasksCursor == null || tomorrowTasksCursor.getCount() <= 0){
            return false;
        }

        return areasCount <= tomorrowTasksCursor.getCount();
    }

    public boolean isQuickTasksLeft() {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor quickTasksCursor = db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_TYPE + " = ?",
                new String[]{ String.valueOf(Task.TYPE_QUICK) },
                null,
                null,
                null);

        return quickTasksCursor != null && quickTasksCursor.getCount() > 0;
    }

    private Cursor getAllTasksForTomorrow(){
        Calendar tomorrow = DateTimeUtils.getTodayDate();
        tomorrow.add(Calendar.DATE, 1);

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_DATE + " = ?",
                new String[]{ String.valueOf(tomorrow.getTimeInMillis()) },
                null,
                null,
                null);
    }

    public long createTask(Task task){
        if(task.isDone()){
            long dateInMilliseconds = isAllPreviousAreaTasksDone(task);
            if(dateInMilliseconds > 0)
                return -2;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        int status;

        if(task.isDone())
            status = 1;
        else
            status = 0;

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME, task.getName());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        if(task.getArea() != null)
            values.put(TaskEntry.COLUMN_AREA_ID, task.getArea().getId());
        else
            values.putNull(TaskEntry.COLUMN_AREA_ID);

        if(task.getDate() != null) {
            values.put(TaskEntry.COLUMN_DATE, task.getDate().getTimeInMillis());
            values.put(TaskEntry.COLUMN_TIME_ZONE, TimeZone.getDefault().getID());
        }
        else {
            values.putNull(TaskEntry.COLUMN_DATE);
            values.putNull(TaskEntry.COLUMN_TIME_ZONE);
        }

        values.put(TaskEntry.COLUMN_STATUS, status);
        values.put(TaskEntry.COLUMN_TYPE, task.getType());

        try {
            long id = db.insert(TaskEntry.TABLE_NAME,
                    null,
                    values);

            setDbNotInitialStatus();

            return id;
        } catch (SQLiteConstraintException e){
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    public long updateTask(Task task){

        if(task.getType() == Task.TYPE_NORMAL) {
            Task taskBeforeUpdate = getTaskByID(task.getId());

            if (!taskBeforeUpdate.isDone() && task.isDone()) {
                long dateInMilliseconds = isAllPreviousAreaTasksDone(taskBeforeUpdate);
                if (dateInMilliseconds > 0)
                    return -2;
            }
        }

        SQLiteDatabase db = this.getWritableDatabase();

        int status;

        if(task.isDone())
            status = 1;
        else
            status = 0;

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME, task.getName());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        if(task.getArea() != null)
            values.put(TaskEntry.COLUMN_AREA_ID, task.getArea().getId());
        else
            values.putNull(TaskEntry.COLUMN_AREA_ID);

        if(task.getDate() != null) {
            values.put(TaskEntry.COLUMN_DATE, task.getDate().getTimeInMillis());
            values.put(TaskEntry.COLUMN_TIME_ZONE, TimeZone.getDefault().getID());
        }
        else {
            values.putNull(TaskEntry.COLUMN_DATE);
            values.putNull(TaskEntry.COLUMN_TIME_ZONE);
        }
        values.put(TaskEntry.COLUMN_STATUS, status);
        values.put(TaskEntry.COLUMN_TYPE, task.getType());

        try{
            long id = db.update(TaskEntry.TABLE_NAME, values, TaskEntry._ID + " = ?",
                    new String[]{ String.valueOf(task.getId()) });

            setDbNotInitialStatus();

            return id;
        }
        catch (SQLiteConstraintException e){
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    public void deleteTask(long task_id){
        deleteAllTaskNotifications(task_id);

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TaskEntry.TABLE_NAME,
                TaskEntry._ID + " = ?",
                new String[]{ String.valueOf(task_id) });
    }

    public long isAllPreviousAreaTasksDone(Task task){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor tasksCursor = db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_AREA_ID + " = ? AND " + TaskEntry.COLUMN_DATE + " < ? AND " + TaskEntry.COLUMN_STATUS + " = ?",
                new String[]{ String.valueOf(task.getArea().getId()), String.valueOf(task.getDate().getTimeInMillis()), String.valueOf(0) },
                null,
                null,
                TaskEntry.COLUMN_DATE + " ASC");

        if(tasksCursor.getCount() == 0)
            return -1;

        tasksCursor.moveToFirst();

        return tasksCursor.getLong(tasksCursor.getColumnIndex(TaskEntry._ID));
    }

    private void deleteAllAreaTasks(long area_id){
        Cursor areaTasksCursor = getAllAreaTasks(area_id);

        while (areaTasksCursor.moveToNext()){
            deleteAllTaskNotifications(areaTasksCursor.getLong(areaTasksCursor.getColumnIndex(TaskEntry._ID)));
        }

        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TaskEntry.TABLE_NAME,
                TaskEntry.COLUMN_AREA_ID + " = ?",
                new String[]{ String.valueOf(area_id) });
    }

    // Notifications

    public Cursor getAllNotifications(){

        SQLiteDatabase db = this.getReadableDatabase();

        return db.query(
                NotificationEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }

    public Notification getNotificationByID(long notification_id){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor notificationCursor = db.query(NotificationEntry.TABLE_NAME,
                null,
                NotificationEntry._ID + " = ?",
                new String[]{ String.valueOf(notification_id) },
                null,
                null,
                null);

        if(notificationCursor != null && notificationCursor.getCount() > 0){
            notificationCursor.moveToFirst();

            Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_DATE)),
                    TimeZone.getTimeZone(notificationCursor.getString(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TIME_ZONE))));

            long taskID = notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TASK_ID));
            Task task = getTaskByID(taskID);

            Notification notification = new Notification(notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry._ID)),
                    notificationCursor.getString(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_MESSAGE)),
                    notificationDate,
                    task,
                    notificationCursor.getInt(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TYPE)));

            return notification;
        }
        else
            return null;
    }

    public Notification getNotificationByMessageAndType(String message, int type){
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor notificationCursor = db.query(NotificationEntry.TABLE_NAME,
                null,
                NotificationEntry.COLUMN_MESSAGE + " = ? AND " + NotificationEntry.COLUMN_TYPE + " = ?",
                new String[]{ message, String.valueOf(type) },
                null,
                null,
                null);

        if(notificationCursor != null && notificationCursor.getCount() > 0){
            notificationCursor.moveToFirst();

            Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_DATE)),
                    TimeZone.getTimeZone(notificationCursor.getString(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TIME_ZONE))));

            long taskID = notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TASK_ID));
            Task task = getTaskByID(taskID);

            Notification notification = new Notification(notificationCursor.getLong(notificationCursor.getColumnIndex(NotificationEntry._ID)),
                    notificationCursor.getString(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_MESSAGE)),
                    notificationDate,
                    task,
                    notificationCursor.getInt(notificationCursor.getColumnIndex(NotificationEntry.COLUMN_TYPE)));

            return notification;
        }
        else
            return null;
    }

    public long createNotification(Notification notification){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotificationEntry.COLUMN_MESSAGE, notification.getMessage());
        values.put(NotificationEntry.COLUMN_DATE, notification.getDate().getTimeInMillis());
        values.put(NotificationEntry.COLUMN_TIME_ZONE, TimeZone.getDefault().getID());
        if(notification.getTask() != null)
            values.put(NotificationEntry.COLUMN_TASK_ID, notification.getTask().getId());
        else
            values.putNull(NotificationEntry.COLUMN_TASK_ID);
        values.put(NotificationEntry.COLUMN_TYPE, notification.getType());

        return db.insert(NotificationEntry.TABLE_NAME,
                null,
                values);
    }

    public Notification createSystemNotification(String message){
        Calendar time = Calendar.getInstance();
        if(message.equals(mContext.getString(R.string.pref_tomorrow_tasks_notification_message)))
            time.set(Calendar.HOUR_OF_DAY, 20);
        else
            if(message.equals(mContext.getString(R.string.pref_unfinished_quick_tasks_message)))
                time.set(Calendar.HOUR_OF_DAY, 18);
        time.set(Calendar.MINUTE, 0);
        time.set(Calendar.SECOND, 0);
        Notification notification = new Notification(message, time, Notification.TYPE_SYSTEM_ID);
        long id = createNotification(notification);

        if(id <= 0)
            return null;

        return notification;
    }

    public long updateNotification(Notification notification){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NotificationEntry.COLUMN_MESSAGE, notification.getMessage());
        values.put(NotificationEntry.COLUMN_DATE, notification.getDate().getTimeInMillis());
        values.put(NotificationEntry.COLUMN_TIME_ZONE, TimeZone.getDefault().getID());
        if(notification.getTask() != null)
            values.put(NotificationEntry.COLUMN_TASK_ID, notification.getTask().getId());
        else
            values.putNull(NotificationEntry.COLUMN_TASK_ID);
        values.put(NotificationEntry.COLUMN_TYPE, notification.getType());


        try{
            long id = db.update(NotificationEntry.TABLE_NAME, values, NotificationEntry._ID + " = ?",
                    new String[]{ String.valueOf(notification.getId()) });

            return id;
        }
        catch (SQLiteConstraintException e){
            Log.e(LOG_TAG, e.getMessage());
            return -1;
        }
    }

    public void deleteNotification(long notification_id){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(NotificationEntry.TABLE_NAME,
                NotificationEntry._ID + " = ?",
                new String[]{ String.valueOf(notification_id) });
    }

    private void deleteAllTaskNotifications(long task_id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor notificationsCursor = db.query(NotificationEntry.TABLE_NAME,
                null,
                NotificationEntry.COLUMN_TASK_ID + " = ?",
                new String[]{ String.valueOf(task_id) },
                null,
                null,
                null);

        if(notificationsCursor != null && notificationsCursor.getCount() > 0){
            while(notificationsCursor.moveToNext()){
                Calendar notificationDate = DateTimeUtils.getInstanceInCurrentTimeZone(notificationsCursor.getLong(notificationsCursor.getColumnIndex(NotificationEntry.COLUMN_DATE)),
                        TimeZone.getTimeZone(notificationsCursor.getString(notificationsCursor.getColumnIndex(NotificationEntry.COLUMN_TIME_ZONE))));

                long taskID = notificationsCursor.getLong(notificationsCursor.getColumnIndex(NotificationEntry.COLUMN_TASK_ID));
                Task task = getTaskByID(taskID);

                Notification notification = new Notification(notificationsCursor.getLong(notificationsCursor.getColumnIndex(NotificationEntry._ID)),
                        notificationsCursor.getString(notificationsCursor.getColumnIndex(NotificationEntry.COLUMN_MESSAGE)),
                        notificationDate,
                        task,
                        notificationsCursor.getInt(notificationsCursor.getColumnIndex(NotificationEntry.COLUMN_TYPE)));

                NotificationUtils.deleteNotification(mContext, notification);
                deleteNotification(notification.getId());
            }
        }
    }

    // SET DATABASE STATUS TO NOT INITIAL
    private void setDbNotInitialStatus(){
        if(mIsDbInitial) {
            AbstractPlannerPreferences.setDatabaseInitialStatus(mContext, false);
            mIsDbInitial = false;
        }
    }

    public void setDbInitialStatus(){
        AbstractPlannerPreferences.setDatabaseInitialStatus(mContext, true);
        mIsDbInitial = true;
    }
}