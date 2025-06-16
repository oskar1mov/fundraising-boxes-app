package com.fundraising.dto;

import com.fundraising.enums.Currency;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateEventRequest {
    @NotBlank(message = "Event name is required")
    private String name;

    @NotNull(message = "Currency is required")
    private Currency currency;

    public CreateEventRequest() {}

    public CreateEventRequest(String name, Currency currency) {
        this.name = name;
        this.currency = currency;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Currency getCurrency() { return currency; }
    public void setCurrency(Currency currency) { this.currency = currency; }
}
