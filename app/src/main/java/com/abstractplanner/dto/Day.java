package com.abstractplanner.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Day {

    // temporary
    private Calendar date;

    private List<Task> tasks;

    public Day(Calendar date) {
        this.date = date;
        tasks = new ArrayList<>();
    }

    public Calendar getDate() {
        return date;
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
