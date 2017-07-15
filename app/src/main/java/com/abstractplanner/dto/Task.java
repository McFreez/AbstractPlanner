package com.abstractplanner.dto;

public class Task {

    private Attribute attribute;
    private String name;
    private String description;
    private boolean done = false;

    public Task(Attribute attribute, String name, String description) {
        this.attribute = attribute;
        this.name = name;
        this.description = description;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
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
