package com.personalassistant.travel;

public enum TripStatus {
    PLANNED,
    ACTIVE,
    COMPLETED,
    CANCELLED;

    public static TripStatus fromString(String value) {
        if (value == null || value.isBlank()) {
            return PLANNED;
        }
        try {
            return TripStatus.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return PLANNED;
        }
    }
}
