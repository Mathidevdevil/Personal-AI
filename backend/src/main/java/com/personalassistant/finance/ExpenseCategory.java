package com.personalassistant.finance;

public enum ExpenseCategory {
    FOOD,
    TRANSPORT,
    SHOPPING,
    BILLS,
    ENTERTAINMENT,
    EDUCATION,
    HEALTH,
    TRAVEL,
    OTHER;

    public static ExpenseCategory fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        try {
            return ExpenseCategory.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
