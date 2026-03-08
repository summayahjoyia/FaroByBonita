// Reminder.java
// Place at: java/com/example/farocaretaker/Reminder.java

package com.example.farocaretaker;

public class Reminder {

    private String  id;
    private String  medicineName;  // maps to medicine_name in RTDB
    private String  time;          // e.g. "14:00"
    private String  frequency;     // e.g. "daily"
    private boolean active;

    public Reminder() {} // required for Firebase

    // ── Getters ────────────────────────────────────────────────────────────
    public String  getId()           { return id; }
    public String  getMedicineName() { return medicineName; }
    public String  getTime()         { return time; }
    public String  getFrequency()    { return frequency; }
    public boolean isActive()        { return active; }

    // ── Setters ────────────────────────────────────────────────────────────
    public void setId(String id)                   { this.id = id; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }
    public void setTime(String time)               { this.time = time; }
    public void setFrequency(String frequency)     { this.frequency = frequency; }
    public void setActive(boolean active)          { this.active = active; }
}