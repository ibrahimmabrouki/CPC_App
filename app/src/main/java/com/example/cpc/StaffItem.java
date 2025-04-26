package com.example.cpc;

public class StaffItem {
    private String id;
    private String name;
    private String type; // doctor, pharmacist, etc.

    public StaffItem(String id, String name, String type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }
}
