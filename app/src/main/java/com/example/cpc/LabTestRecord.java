package com.example.cpc;

public class LabTestRecord {
    public int recordId;
    public int patientId;
    public int doctorId;
    public String content;

    public LabTestRecord(int recordId, int patientId, int doctorId, String content) {
        this.recordId = recordId;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.content = content;
    }
}
