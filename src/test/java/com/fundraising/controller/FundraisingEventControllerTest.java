package com.fundraising.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundraising.dto.CreateEventRequest;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.enums.Currency;
import com.fundraising.exception.DuplicateEventNameException;
import com.fundraising.service.FundraisingEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FundraisingEventController.class)
class FundraisingEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FundraisingEventService fundraisingEventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldCreateEventSuccessfully() throws Exception {
        // Given
        CreateEventRequest request = new CreateEventRequest("Charity Run", Currency.USD);
        FundraisingEventDTO responseDto = new FundraisingEventDTO(1L, "Charity Run", BigDecimal.ZERO, Currency.USD);

        when(fundraisingEventService.createEvent(any(CreateEventRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Charity Run"))
                .andExpect(jsonPath("$.balance").value(0))
                .andExpect(jsonPath("$.currency").value("USD"));

        verify(fundraisingEventService).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void shouldReturnConflictWhenCreatingDuplicateEvent() throws Exception {
        // Given
        CreateEventRequest request = new CreateEventRequest("Existing Event", Currency.EUR);
        when(fundraisingEventService.createEvent(any(CreateEventRequest.class)))
                .thenThrow(new DuplicateEventNameException("An event with the name 'Existing Event' already exists"));

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("An event with the name 'Existing Event' already exists"));

        verify(fundraisingEventService).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidEventRequest() throws Exception {
        // Given
        CreateEventRequest request = new CreateEventRequest("", Currency.USD); // Empty name

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(fundraisingEventService, never()).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void shouldGetAllEventsSuccessfully() throws Exception {
        // Given
        List<FundraisingEventDTO> events = Arrays.asList(
                new FundraisingEventDTO(1L, "Charity Run", new BigDecimal("500.00"), Currency.USD),
                new FundraisingEventDTO(2L, "Bake Sale", new BigDecimal("150.50"), Currency.EUR)
        );

        when(fundraisingEventService.getAllEvents()).thenReturn(events);

        // When & Then
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Charity Run"))
                .andExpect(jsonPath("$[0].balance").value(500.00))
                .andExpect(jsonPath("$[1].name").value("Bake Sale"))
                .andExpect(jsonPath("$[1].balance").value(150.50));

        verify(fundraisingEventService).getAllEvents();
    }

    @Test
    void shouldGetEventByIdSuccessfully() throws Exception {
        // Given
        Long eventId = 1L;
        FundraisingEventDTO eventDto = new FundraisingEventDTO(eventId, "Test Event", new BigDecimal("200.00"), Currency.GBP);

        when(fundraisingEventService.getEventById(eventId)).thenReturn(Optional.of(eventDto));

        // When & Then
        mockMvc.perform(get("/api/events/{id}", eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eventId))
                .andExpect(jsonPath("$.name").value("Test Event"))
                .andExpect(jsonPath("$.balance").value(200.00))
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(fundraisingEventService).getEventById(eventId);
    }

    @Test
    void shouldReturnNotFoundWhenEventDoesNotExist() throws Exception {
        // Given
        Long nonExistentId = 999L;
        when(fundraisingEventService.getEventById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/events/{id}", nonExistentId))
                .andExpect(status().isNotFound());

        verify(fundraisingEventService).getEventById(nonExistentId);
    }

    @Test
    void shouldReturnEmptyListWhenNoEventsExist() throws Exception {
        // Given
        when(fundraisingEventService.getAllEvents()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));

        verify(fundraisingEventService).getAllEvents();
    }

    @Test
    void shouldHandleNullCurrencyInRequest() throws Exception {
        // Given - Request with null currency should fail validation
        String invalidJson = """
            {
                "name": "Test Event",
                "currency": null
            }
            """;

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(fundraisingEventService, never()).createEvent(any(CreateEventRequest.class));
    }

    @Test
    void shouldCreateEventWithDifferentCurrencies() throws Exception {
        // Given
        CreateEventRequest gbpRequest = new CreateEventRequest("UK Fundraiser", Currency.GBP);
        FundraisingEventDTO gbpResponse = new FundraisingEventDTO(1L, "UK Fundraiser", BigDecimal.ZERO, Currency.GBP);

        when(fundraisingEventService.createEvent(any(CreateEventRequest.class))).thenReturn(gbpResponse);

        // When & Then
        mockMvc.perform(post("/api/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(gbpRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("GBP"));

        verify(fundraisingEventService).createEvent(any(CreateEventRequest.class));
    }
}