package com.abstractplanner.dto;

import java.util.Calendar;

public class Task {

    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_QUICK = 2;

    private long id;
    private Area area;
    private String name;
    private String description;
    private Calendar date;
    private boolean done = false;
    private int type;

    public Task() {
    }

    public Task(Area area, String name, String description, Calendar date, boolean done, int type) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.date = date;
        this.done = done;
        this.type = type;
    }

    public Task(long id, Area area, String name, String description, Calendar date, boolean done, int type) {
        this.id = id;
        this.area = area;
        this.name = name;
        this.description = description;
        this.date = date;
        this.done = done;
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Area getArea() {
        return area;
    }

    public void setArea(Area area) {
        this.area = area;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
