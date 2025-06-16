package com.fundraising.dto;

public class BoxDto {
    private Long id;  // Keep for REST operations (POST response, DELETE by ID)
    private String boxIdentifier;
    private boolean assigned;
    private boolean empty;

    public BoxDto() {}

    public BoxDto(Long id, String boxIdentifier, boolean assigned, boolean empty) {
        this.id = id;
        this.boxIdentifier = boxIdentifier;
        this.assigned = assigned;
        this.empty = empty;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBoxIdentifier() { return boxIdentifier; }
    public void setBoxIdentifier(String boxIdentifier) { this.boxIdentifier = boxIdentifier; }

    public boolean isAssigned() { return assigned; }
    public void setAssigned(boolean assigned) { this.assigned = assigned; }

    public boolean isEmpty() { return empty; }
    public void setEmpty(boolean empty) { this.empty = empty; }
}
