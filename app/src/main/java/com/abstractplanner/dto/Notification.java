package com.abstractplanner.dto;

import java.util.Calendar;

public class Notification {

    public static final int TYPE_ONE_TIME_ID = 1;
    public static final String TYPE_ONE_TIME_NAME = "One time";
    public static final int TYPE_EVERY_DAY_ID = 2;
    public static final String TYPE_EVERY_DAY_NAME = "Every day";

    public static int getNotificationTypeID(String name){
        switch (name){
            case TYPE_ONE_TIME_NAME:
                return TYPE_ONE_TIME_ID;
            case TYPE_EVERY_DAY_NAME:
                return TYPE_EVERY_DAY_ID;
            default:
                return TYPE_ONE_TIME_ID;
        }
    }

    public static String getNotificationTypeName(int id){
        switch (id){
            case TYPE_ONE_TIME_ID:
                return TYPE_ONE_TIME_NAME;
            case TYPE_EVERY_DAY_ID:
                return TYPE_EVERY_DAY_NAME;
            default:
                return TYPE_ONE_TIME_NAME;
        }
    }

    private long id;
    private String message;
    private Calendar date;
    private Task task;
    private int type;

    public Notification(long id, String message, Calendar date, Task task, int type) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.task = task;
        this.type = type;
    }

    public Notification(String message, Calendar date, Task task, int type) {
        this.message = message;
        this.date = date;
        this.task = task;
        this.type = type;
    }

    public Notification(long id, String message, Calendar date, int type) {
        this.id = id;
        this.message = message;
        this.date = date;
        this.type = type;
    }

    public Notification(String message, Calendar date, int type) {
        this.message = message;
        this.date = date;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
