package com.abstractplanner.dto;

import java.util.Calendar;

public class Task {

    private long id;
    private Area area;
    private String name;
    private String description;
    private Calendar date;
    private boolean done = false;
    private int viewType;

    public Task() {
    }

    public Task(Area area, String name, String description, Calendar date, boolean done) {
        this.area = area;
        this.name = name;
        this.description = description;
        this.date = date;
        this.viewType = 0;
    }

    public Task(long id, Area area, String name, String description, Calendar date, boolean done) {
        this.id = id;
        this.area = area;
        this.name = name;
        this.description = description;
        this.date = date;
        this.done = done;
        this.viewType = 0;
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

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }
}
