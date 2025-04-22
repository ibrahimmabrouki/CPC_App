package com.example.cpc;

public class Patient {
    public String name;
    public String id;
    public String reason;
    public int appointmentId;

    public Patient(String name, String id, String reason, int appointmentId) {
        this.name = name;
        this.id = id;
        this.reason = reason;
        this.appointmentId = appointmentId;
    }
}
