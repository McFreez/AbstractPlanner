package com.abstractplanner.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.abstractplanner.data.AbstractPlannerContract.*;
import com.abstractplanner.dto.Area;
import com.abstractplanner.dto.Task;

import java.util.Calendar;

public class AbstractPlannerDatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG_tAG = AbstractPlannerDatabaseHelper.class.getName();

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "abstractPlanner.db";

    public AbstractPlannerDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String CREATE_TABLE_AREA = "CREATE TABLE " + AreaEntry.TABLE_NAME + " ("
                + AreaEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + AreaEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + AreaEntry.COLUMN_DESCRIPTION + " TEXT,"
                + " UNIQUE (" + AreaEntry.COLUMN_NAME + ") ON CONFLICT FAIL);";

        final String CREATE_TABLE_TASK = "CREATE TABLE " + TaskEntry.TABLE_NAME + " ("
                + TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + TaskEntry.COLUMN_NAME + " TEXT NOT NULL,"
                + TaskEntry.COLUMN_DESCRIPTION + " TEXT,"
                + TaskEntry.COLUMN_AREA_ID + " INTEGER NOT NULL,"
                + TaskEntry.COLUMN_DATE + " INTEGER NOT NULL,"
                + TaskEntry.COLUMN_STATUS + " INTEGER NOT NULL,"
                + " UNIQUE (" + TaskEntry.COLUMN_AREA_ID + "," + TaskEntry.COLUMN_DATE + ") ON CONFLICT FAIL);";

        db.execSQL(CREATE_TABLE_AREA);
        db.execSQL(CREATE_TABLE_TASK);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + AreaEntry.TABLE_NAME);

        onCreate(db);
    }

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

        return db.insert(AreaEntry.TABLE_NAME, null, values);
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
            return id;
        }
        catch (SQLiteConstraintException e){
            Log.e(LOG_tAG, e.getMessage());
            return -1;
        }
    }

    public void deleteArea(long area_id){
        deleteAllAreaTasks(area_id);
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(AreaEntry.TABLE_NAME, AreaEntry._ID + " = ?",
                new String[]{ String.valueOf(area_id) });
    }

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
        SQLiteDatabase db = this.getReadableDatabase();

        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        return db.query(TaskEntry.TABLE_NAME,
                null,
                TaskEntry.COLUMN_STATUS + " = ? AND " + TaskEntry.COLUMN_DATE + " <= ?",
                new String[]{String.valueOf(0), String.valueOf(today.getTimeInMillis())},
                null,
                null,
                TaskEntry.COLUMN_DATE + " ASC");
    }

    public long createTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();

        int status;

        if(task.isDone())
            status = 1;
        else
            status = 0;

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME, task.getName());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskEntry.COLUMN_AREA_ID, task.getArea().getId());
        values.put(TaskEntry.COLUMN_DATE, task.getDate().getTimeInMillis());
        values.put(TaskEntry.COLUMN_STATUS, status);

        return db.insert(TaskEntry.TABLE_NAME,
                null,
                values);
    }

    public long updateTask(Task task){
        SQLiteDatabase db = this.getWritableDatabase();

        int status;

        if(task.isDone())
            status = 1;
        else
            status = 0;

        ContentValues values = new ContentValues();
        values.put(TaskEntry.COLUMN_NAME, task.getName());
        values.put(TaskEntry.COLUMN_DESCRIPTION, task.getDescription());
        values.put(TaskEntry.COLUMN_AREA_ID, task.getArea().getId());
        values.put(TaskEntry.COLUMN_DATE, task.getDate().getTimeInMillis());
        values.put(TaskEntry.COLUMN_STATUS, status);

        try{
            long id = db.update(TaskEntry.TABLE_NAME, values, TaskEntry._ID + " = ?",
                    new String[]{ String.valueOf(task.getId()) });

            return id;
        }
        catch (SQLiteConstraintException e){
            Log.e(LOG_tAG, e.getMessage());
            return -1;
        }
    }

    private void deleteAllAreaTasks(long area_id){
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete(TaskEntry.TABLE_NAME,
                TaskEntry.COLUMN_AREA_ID + " = ?",
                new String[]{ String.valueOf(area_id) });
    }

}