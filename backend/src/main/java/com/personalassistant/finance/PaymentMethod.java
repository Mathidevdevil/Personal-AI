package com.personalassistant.finance;

public enum PaymentMethod {
    CASH,
    UPI,
    CARD,
    BANK_TRANSFER,
    OTHER;

    public static PaymentMethod fromString(String value) {
        if (value == null || value.isBlank()) {
            return OTHER;
        }
        try {
            return PaymentMethod.valueOf(value.toUpperCase().trim().replace(" ", "_"));
        } catch (IllegalArgumentException e) {
            return OTHER;
        }
    }
}
