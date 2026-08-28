package com.personalassistant.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateCurrencyRequest {
    @NotBlank(message = "Currency cannot be empty")
    @Size(min = 2, max = 10, message = "Currency code must be valid (e.g. INR, USD, EUR)")
    private String currency;
}
