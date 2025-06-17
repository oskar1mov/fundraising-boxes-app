package com.fundraising.service;

import com.fundraising.dto.CreateEventRequest;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.entity.FundraisingEvent;
import com.fundraising.enums.Currency;
import com.fundraising.exception.DuplicateEventNameException;
import com.fundraising.mapper.FundraisingEventMapper;
import com.fundraising.repository.FundraisingEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FundraisingEventServiceTest {

    @Mock
    private FundraisingEventRepository repository;

    @Mock
    private FundraisingEventMapper mapper;

    private FundraisingEventService service;

    @BeforeEach
    void setUp() {
        service = new FundraisingEventService();
        // Use reflection to set private fields for testing
        try {
            java.lang.reflect.Field repoField = FundraisingEventService.class.getDeclaredField("fundraisingEventRepository");
            repoField.setAccessible(true);
            repoField.set(service, repository);

            java.lang.reflect.Field mapperField = FundraisingEventService.class.getDeclaredField("mapper");
            mapperField.setAccessible(true);
            mapperField.set(service, mapper);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set up test", e);
        }
    }

    @Test
    void shouldCreateEventSuccessfully() {
        // Given
        CreateEventRequest request = new CreateEventRequest("Charity Run", Currency.USD);
        FundraisingEvent entity = new FundraisingEvent("Charity Run", Currency.USD);
        entity.setId(1L);
        FundraisingEventDTO expectedDto = new FundraisingEventDTO(1L, "Charity Run", BigDecimal.ZERO, Currency.USD);

        when(repository.existsByNameIgnoreCase("Charity Run")).thenReturn(false);
        when(mapper.toEntity(request)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toDto(entity)).thenReturn(expectedDto);

        // When
        FundraisingEventDTO result = service.createEvent(request);

        // Then
        assertNotNull(result);
        assertEquals("Charity Run", result.getName());
        assertEquals(Currency.USD, result.getCurrency());
        assertEquals(BigDecimal.ZERO, result.getBalance());

        verify(repository).existsByNameIgnoreCase("Charity Run");
        verify(mapper).toEntity(request);
        verify(repository).save(entity);
        verify(mapper).toDto(entity);
    }

    @Test
    void shouldThrowExceptionWhenEventNameAlreadyExists() {
        // Given
        CreateEventRequest request = new CreateEventRequest("Existing Event", Currency.EUR);
        when(repository.existsByNameIgnoreCase("Existing Event")).thenReturn(true);

        // When & Then
        DuplicateEventNameException exception = assertThrows(
                DuplicateEventNameException.class,
                () -> service.createEvent(request)
        );

        assertEquals("An event with the name 'Existing Event' already exists", exception.getMessage());
        verify(repository).existsByNameIgnoreCase("Existing Event");
        verify(mapper, never()).toEntity(any());
        verify(repository, never()).save(any());
    }

    @Test
    void shouldReturnAllEvents() {
        // Given
        FundraisingEvent event1 = new FundraisingEvent("Event 1", Currency.USD);
        event1.setId(1L);
        FundraisingEvent event2 = new FundraisingEvent("Event 2", Currency.EUR);
        event2.setId(2L);

        List<FundraisingEvent> entities = Arrays.asList(event1, event2);

        FundraisingEventDTO dto1 = new FundraisingEventDTO(1L, "Event 1", BigDecimal.ZERO, Currency.USD);
        FundraisingEventDTO dto2 = new FundraisingEventDTO(2L, "Event 2", BigDecimal.ZERO, Currency.EUR);
        List<FundraisingEventDTO> expectedDtos = Arrays.asList(dto1, dto2);

        when(repository.findAll()).thenReturn(entities);
        when(mapper.toDtoList(entities)).thenReturn(expectedDtos);

        // When
        List<FundraisingEventDTO> result = service.getAllEvents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Event 1", result.get(0).getName());
        assertEquals("Event 2", result.get(1).getName());

        verify(repository).findAll();
        verify(mapper).toDtoList(entities);
    }

    @Test
    void shouldReturnEventById() {
        // Given
        Long eventId = 1L;
        FundraisingEvent entity = new FundraisingEvent("Test Event", Currency.GBP);
        entity.setId(eventId);
        FundraisingEventDTO expectedDto = new FundraisingEventDTO(eventId, "Test Event", BigDecimal.ZERO, Currency.GBP);

        when(repository.findById(eventId)).thenReturn(Optional.of(entity));
        when(mapper.toDto(entity)).thenReturn(expectedDto);

        // When
        Optional<FundraisingEventDTO> result = service.getEventById(eventId);

        // Then
        assertTrue(result.isPresent());
        assertEquals("Test Event", result.get().getName());
        assertEquals(Currency.GBP, result.get().getCurrency());

        verify(repository).findById(eventId);
        verify(mapper).toDto(entity);
    }

    @Test
    void shouldReturnEmptyOptionalWhenEventNotFound() {
        // Given
        Long nonExistentId = 999L;
        when(repository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When
        Optional<FundraisingEventDTO> result = service.getEventById(nonExistentId);

        // Then
        assertFalse(result.isPresent());
        verify(repository).findById(nonExistentId);
        verify(mapper, never()).toDto(any());
    }

    @Test
    void shouldHandleCaseInsensitiveNameCheck() {
        // Given
        CreateEventRequest request = new CreateEventRequest("charity run", Currency.USD);
        when(repository.existsByNameIgnoreCase("charity run")).thenReturn(true);

        // When & Then
        assertThrows(DuplicateEventNameException.class, () -> service.createEvent(request));
        verify(repository).existsByNameIgnoreCase("charity run");
    }
}