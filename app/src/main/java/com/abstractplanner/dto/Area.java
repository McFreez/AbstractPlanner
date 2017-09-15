package com.abstractplanner.dto;



public class Area {

    public static final int ARCHIVED = 1;
    public static final int NOT_ARCHIVED = 0;

    private long id;
    private String name;
    private String description;
    private boolean archived;

    public Area(String name, String description) {
        this.name = name;
        this.description = description;
        this.archived = false;
    }

    public Area(String name, String description, boolean archived) {
        this.name = name;
        this.description = description;
        this.archived = archived;
    }

    public Area(long id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.archived = false;
    }

    public Area(long id, String name, String description, boolean archived) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.archived = archived;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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

    public boolean isArchived() {
        return archived;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }
}
