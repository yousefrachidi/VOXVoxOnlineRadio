package com.youra.radiofr.item;

public class ItemNotification {

    private final String id;
    private final String name;
    private final String not;
    private final String date;

    public ItemNotification(String id, String name, String not, String date) {
        this.id = id;
        this.name = name;
        this.not = not;
        this.date = date;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getNot() {
        return not;
    }

    public String getDate() {
        return date;
    }
}
