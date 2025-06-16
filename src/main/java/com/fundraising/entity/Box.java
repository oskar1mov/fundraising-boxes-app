package com.fundraising.entity;

import com.fundraising.enums.BoxStatus;
import jakarta.persistence.*;

@Entity
@Table(name = "boxes")
public class Box {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "box_identifier", nullable = false, unique = true)
    private String boxIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BoxStatus status = BoxStatus.AVAILABLE;

    @ManyToOne
    @JoinColumn(name = "assigned_event_id")
    private FundraisingEvent assignedEvent;

    public Box() {}

    public Box(String boxIdentifier) {
        this.boxIdentifier = boxIdentifier;
        this.status = BoxStatus.AVAILABLE;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getBoxIdentifier() { return boxIdentifier; }
    public void setBoxIdentifier(String boxIdentifier) { this.boxIdentifier = boxIdentifier; }

    public BoxStatus getStatus() { return status; }
    public void setStatus(BoxStatus status) { this.status = status; }

    public FundraisingEvent getAssignedEvent() { return assignedEvent; }
    public void setAssignedEvent(FundraisingEvent assignedEvent) { this.assignedEvent = assignedEvent; }
}