package com.fundraising.controller;

import com.fundraising.dto.CreateEventRequest;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.exception.DuplicateEventNameException;
import com.fundraising.service.FundraisingEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class FundraisingEventController {

    @Autowired
    private FundraisingEventService fundraisingEventService;

    @PostMapping
    public ResponseEntity<FundraisingEventDTO> createEvent(@Valid @RequestBody CreateEventRequest request) {
        FundraisingEventDTO event = fundraisingEventService.createEvent(request);
        return ResponseEntity.ok(event);
    }

    @GetMapping
    public ResponseEntity<List<FundraisingEventDTO>> getAllEvents() {
        List<FundraisingEventDTO> events = fundraisingEventService.getAllEvents();
        return ResponseEntity.ok(events);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FundraisingEventDTO> getEventById(@PathVariable Long id) {
        return fundraisingEventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @ExceptionHandler(DuplicateEventNameException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateEventName(DuplicateEventNameException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }
}