package com.fundraising.controller;

import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.exception.BoxNotFoundException;
import com.fundraising.exception.DuplicateBoxIdentifierException;
import com.fundraising.service.BoxService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/boxes")
public class BoxController {

    private final BoxService boxService;

    public BoxController(BoxService boxService) {
        this.boxService = boxService;
    }

    @PostMapping
    public ResponseEntity<BoxDto> registerBox(@Valid @RequestBody CreateBoxRequest request) {
        BoxDto box = boxService.registerBox(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unregisterBox(@PathVariable Long id) {
        boxService.unregisterBox(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<BoxDto>> getAllBoxes() {
        List<BoxDto> boxes = boxService.getAllBoxes();
        return ResponseEntity.ok(boxes);
    }

    @ExceptionHandler(DuplicateBoxIdentifierException.class)
    public ResponseEntity<Map<String, String>> handleDuplicateBoxIdentifier(DuplicateBoxIdentifierException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(BoxNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleBoxNotFound(BoxNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", ex.getMessage()));
    }
}