package com.abstractplanner.dto;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class Day {

    // temporary
    private String date;

    private List<Task> tasks;

    public Day(String date) {
        this.date = date;
        tasks = new ArrayList<>();
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(Task task) {
        this.tasks.add(task);
    }
}
