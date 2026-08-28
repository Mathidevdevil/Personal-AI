package com.personalassistant.task;

public enum TaskStatus {
    TODO,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED;

    public static TaskStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return TODO;
        }
        try {
            return TaskStatus.valueOf(value.toUpperCase().trim().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return TODO;
        }
    }
}
