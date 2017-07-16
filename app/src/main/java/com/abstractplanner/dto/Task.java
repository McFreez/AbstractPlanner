package com.abstractplanner.dto;

public class Task {

    private Area area;
    private String name;
    private String description;
    private boolean done = false;

    public Task(Area area, String name, String description) {
        this.area = area;
        this.name = name;
        this.description = description;
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

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }
}
