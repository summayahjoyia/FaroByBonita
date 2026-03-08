package com.example.farocaretaker; // change this to match your actual package name

public class Reminder {

    private String id;
    private String medicineName;
    private String time;
    private String frequency;

    // Empty constructor required by Firebase
    public Reminder() {}

    public Reminder(String medicineName, String time, String frequency) {
        this.medicineName = medicineName;
        this.time = time;
        this.frequency = frequency;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getFrequency() { return frequency; }
    public void setFrequency(String frequency) { this.frequency = frequency; }
}