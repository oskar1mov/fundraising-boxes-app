package com.fundraising.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fundraising.dto.AddMoneyRequest;
import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.enums.Currency;
import com.fundraising.exception.BoxNotFoundException;
import com.fundraising.exception.DuplicateBoxIdentifierException;
import com.fundraising.service.BoxService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoxController.class)
class BoxControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BoxService boxService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRegisterBoxSuccessfully() throws Exception {
        // Given
        CreateBoxRequest request = new CreateBoxRequest("BOX-001");
        BoxDto responseDto = new BoxDto(1L, "BOX-001", false, true);

        when(boxService.registerBox(any(CreateBoxRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.boxIdentifier").value("BOX-001"))
                .andExpect(jsonPath("$.assigned").value(false))
                .andExpect(jsonPath("$.empty").value(true));

        verify(boxService).registerBox(any(CreateBoxRequest.class));
    }

    @Test
    void shouldReturnConflictWhenRegisteringDuplicateBox() throws Exception {
        // Given
        CreateBoxRequest request = new CreateBoxRequest("BOX-001");
        when(boxService.registerBox(any(CreateBoxRequest.class)))
                .thenThrow(new DuplicateBoxIdentifierException("Box with identifier 'BOX-001' already exists"));

        // When & Then
        mockMvc.perform(post("/api/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Box with identifier 'BOX-001' already exists"));

        verify(boxService).registerBox(any(CreateBoxRequest.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidBoxRequest() throws Exception {
        // Given
        CreateBoxRequest request = new CreateBoxRequest(""); // Empty identifier

        // When & Then
        mockMvc.perform(post("/api/boxes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(boxService, never()).registerBox(any(CreateBoxRequest.class));
    }

    @Test
    void shouldUnregisterBoxSuccessfully() throws Exception {
        // Given
        Long boxId = 1L;
        doNothing().when(boxService).unregisterBox(boxId);

        // When & Then
        mockMvc.perform(delete("/api/boxes/{id}", boxId))
                .andExpect(status().isNoContent());

        verify(boxService).unregisterBox(boxId);
    }

    @Test
    void shouldReturnNotFoundWhenUnregisteringNonExistentBox() throws Exception {
        // Given
        Long boxId = 999L;
        doThrow(new BoxNotFoundException("Box with ID 999 not found"))
                .when(boxService).unregisterBox(boxId);

        // When & Then
        mockMvc.perform(delete("/api/boxes/{id}", boxId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Box with ID 999 not found"));

        verify(boxService).unregisterBox(boxId);
    }

    @Test
    void shouldGetAllBoxesSuccessfully() throws Exception {
        // Given
        List<BoxDto> boxes = Arrays.asList(
                new BoxDto(1L, "BOX-001", false, true),
                new BoxDto(2L, "BOX-002", true, false)
        );

        when(boxService.getAllBoxes()).thenReturn(boxes);

        // When & Then
        mockMvc.perform(get("/api/boxes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].boxIdentifier").value("BOX-001"))
                .andExpect(jsonPath("$[1].boxIdentifier").value("BOX-002"));

        verify(boxService).getAllBoxes();
    }

    @Test
    void shouldAssignBoxToEventSuccessfully() throws Exception {
        // Given
        Long boxId = 1L;
        Long eventId = 1L;
        BoxDto responseDto = new BoxDto(boxId, "BOX-001", true, true);

        when(boxService.assignBoxToEvent(boxId, eventId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/boxes/{boxId}/assign/{eventId}", boxId, eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boxId))
                .andExpect(jsonPath("$.assigned").value(true));

        verify(boxService).assignBoxToEvent(boxId, eventId);
    }

    @Test
    void shouldReturnBadRequestWhenAssigningAlreadyAssignedBox() throws Exception {
        // Given
        Long boxId = 1L;
        Long eventId = 1L;
        when(boxService.assignBoxToEvent(boxId, eventId))
                .thenThrow(new IllegalStateException("Box is already assigned to another event"));

        // When & Then
        mockMvc.perform(put("/api/boxes/{boxId}/assign/{eventId}", boxId, eventId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Box is already assigned to another event"));

        verify(boxService).assignBoxToEvent(boxId, eventId);
    }

    @Test
    void shouldUnassignBoxFromEventSuccessfully() throws Exception {
        // Given
        Long boxId = 1L;
        BoxDto responseDto = new BoxDto(boxId, "BOX-001", false, true);

        when(boxService.unassignBoxFromEvent(boxId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(put("/api/boxes/{boxId}/unassign", boxId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boxId))
                .andExpect(jsonPath("$.assigned").value(false));

        verify(boxService).unassignBoxFromEvent(boxId);
    }

    @Test
    void shouldAddMoneyToBoxSuccessfully() throws Exception {
        // Given
        Long boxId = 1L;
        AddMoneyRequest request = new AddMoneyRequest(Currency.USD, new BigDecimal("50.00"));
        BoxDto responseDto = new BoxDto(boxId, "BOX-001", true, false);

        when(boxService.addMoneyToBox(eq(boxId), any(AddMoneyRequest.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/boxes/{boxId}/money", boxId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boxId))
                .andExpect(jsonPath("$.empty").value(false));

        verify(boxService).addMoneyToBox(eq(boxId), any(AddMoneyRequest.class));
    }

    @Test
    void shouldReturnBadRequestForInvalidMoneyAmount() throws Exception {
        // Given
        Long boxId = 1L;
        AddMoneyRequest request = new AddMoneyRequest(Currency.USD, new BigDecimal("-10.00")); // Negative amount

        // When & Then
        mockMvc.perform(post("/api/boxes/{boxId}/money", boxId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(boxService, never()).addMoneyToBox(any(), any());
    }

    @Test
    void shouldEmptyBoxSuccessfully() throws Exception {
        // Given
        Long boxId = 1L;
        BoxDto responseDto = new BoxDto(boxId, "BOX-001", true, true);

        when(boxService.emptyBox(boxId)).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/boxes/{boxId}/empty", boxId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boxId))
                .andExpect(jsonPath("$.empty").value(true));

        verify(boxService).emptyBox(boxId);
    }

    @Test
    void shouldReturnBadRequestWhenEmptyingUnassignedBox() throws Exception {
        // Given
        Long boxId = 1L;
        when(boxService.emptyBox(boxId))
                .thenThrow(new IllegalStateException("Box must be assigned to a fundraising event before emptying"));

        // When & Then
        mockMvc.perform(post("/api/boxes/{boxId}/empty", boxId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Box must be assigned to a fundraising event before emptying"));

        verify(boxService).emptyBox(boxId);
    }

    @Test
    void shouldHandleRuntimeExceptionGracefully() throws Exception {
        // Given
        Long boxId = 1L;
        when(boxService.emptyBox(boxId))
                .thenThrow(new RuntimeException("Unexpected error occurred"));

        // When & Then
        mockMvc.perform(post("/api/boxes/{boxId}/empty", boxId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Unexpected error occurred"));

        verify(boxService).emptyBox(boxId);
    }
}