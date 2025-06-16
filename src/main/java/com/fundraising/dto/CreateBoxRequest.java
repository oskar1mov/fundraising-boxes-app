package com.fundraising.dto;

import jakarta.validation.constraints.NotBlank;

public class CreateBoxRequest {
    @NotBlank(message = "Box identifier is required")
    private String boxIdentifier;

    public CreateBoxRequest() {}

    public CreateBoxRequest(String boxIdentifier) {
        this.boxIdentifier = boxIdentifier;
    }

    public String getBoxIdentifier() { return boxIdentifier; }
    public void setBoxIdentifier(String boxIdentifier) { this.boxIdentifier = boxIdentifier; }
}