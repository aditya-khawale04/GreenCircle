package com.example.greencircle;

import com.google.firebase.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class Task {
    private String taskId; // Document ID
    private String userId;
    private String gardenPlantId;
    private String taskType; // "Watering", "Fertilizing", "Pruning"
    private Timestamp dueTime; // Firestore Timestamp
    private boolean isCompleted;

    // Empty constructor required for Firestore
    public Task() {}

    // Getters
    public String getTaskId() { return taskId; }
    public void setTaskId(String taskId) { this.taskId = taskId; }

    public String getUserId() { return userId; }
    public String getGardenPlantId() { return gardenPlantId; }
    public String getTaskType() { return taskType; }
    public Timestamp getDueTime() { return dueTime; }
    public boolean isCompleted() { return isCompleted; }

    // Helper to format date for UI (e.g., "Jan 12, 02:30 PM")
    public String getFormattedDueTime() {
        if (dueTime == null) return "No due date";
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault());
        return sdf.format(dueTime.toDate());
    }
    // Inside Task.java
    public void setCompleted(boolean completed) { isCompleted = completed; }
}