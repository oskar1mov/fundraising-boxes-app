package com.fundraising.service;

import com.fundraising.dto.AddMoneyRequest;
import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.entity.Box;
import com.fundraising.entity.BoxCurrency;
import com.fundraising.entity.FundraisingEvent;
import com.fundraising.enums.BoxStatus;
import com.fundraising.enums.Currency;
import com.fundraising.exception.BoxNotFoundException;
import com.fundraising.exception.DuplicateBoxIdentifierException;
import com.fundraising.mapper.BoxMapper;
import com.fundraising.repository.BoxCurrencyRepository;
import com.fundraising.repository.BoxRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BoxServiceTest {

    @Mock
    private BoxRepository boxRepository;

    @Mock
    private BoxCurrencyRepository boxCurrencyRepository;

    @Mock
    private FundraisingEventRepository fundraisingEventRepository;

    @Mock
    private BoxMapper boxMapper;

    @Mock
    private CurrencyConverter currencyConverter;

    private BoxService service;

    @BeforeEach
    void setUp() {
        service = new BoxService(
                boxRepository,
                boxCurrencyRepository,
                fundraisingEventRepository,
                boxMapper,
                currencyConverter
        );
    }

    @Test
    void shouldRegisterBoxSuccessfully() {
        // Given
        CreateBoxRequest request = new CreateBoxRequest("BOX-001");
        Box box = new Box("BOX-001");
        box.setId(1L);
        BoxDto expectedDto = new BoxDto(1L, "BOX-001", false, true);

        when(boxRepository.existsByBoxIdentifier("BOX-001")).thenReturn(false);
        when(boxMapper.toEntity(request)).thenReturn(box);
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.registerBox(request);

        // Then
        assertNotNull(result);
        assertEquals("BOX-001", result.getBoxIdentifier());
        assertFalse(result.isAssigned());
        assertTrue(result.isEmpty());

        verify(boxRepository).existsByBoxIdentifier("BOX-001");
        verify(boxMapper).toEntity(request);
        verify(boxRepository).save(box);
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldThrowExceptionWhenBoxIdentifierAlreadyExists() {
        // Given
        CreateBoxRequest request = new CreateBoxRequest("BOX-001");
        when(boxRepository.existsByBoxIdentifier("BOX-001")).thenReturn(true);

        // When & Then
        DuplicateBoxIdentifierException exception = assertThrows(
                DuplicateBoxIdentifierException.class,
                () -> service.registerBox(request)
        );

        assertEquals("Box with identifier 'BOX-001' already exists", exception.getMessage());
        verify(boxRepository).existsByBoxIdentifier("BOX-001");
        verify(boxMapper, never()).toEntity(any());
        verify(boxRepository, never()).save(any());
    }

    @Test
    void shouldUnregisterBoxSuccessfully() {
        // Given
        Long boxId = 1L;
        Box box = new Box("BOX-001");
        box.setId(boxId);
        List<BoxCurrency> currencies = Arrays.asList(
                new BoxCurrency(box, Currency.USD, new BigDecimal("10.00"))
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxCurrencyRepository.findByBox(box)).thenReturn(currencies);

        // When
        service.unregisterBox(boxId);

        // Then
        verify(boxRepository).findById(boxId);
        verify(boxCurrencyRepository).findByBox(box);
        verify(boxCurrencyRepository).deleteAll(currencies);
        verify(boxRepository).delete(box);
    }

    @Test
    void shouldThrowExceptionWhenUnregisteringNonExistentBox() {
        // Given
        Long nonExistentId = 999L;
        when(boxRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // When & Then
        BoxNotFoundException exception = assertThrows(
                BoxNotFoundException.class,
                () -> service.unregisterBox(nonExistentId)
        );

        assertEquals("Box with ID 999 not found", exception.getMessage());
        verify(boxRepository).findById(nonExistentId);
        verify(boxCurrencyRepository, never()).findByBox(any());
        verify(boxRepository, never()).delete(any());
    }

    @Test
    void shouldAssignBoxToEventSuccessfully() {
        // Given
        Long boxId = 1L;
        Long eventId = 1L;
        Box box = new Box("BOX-001");
        box.setId(boxId);
        box.setStatus(BoxStatus.AVAILABLE);

        FundraisingEvent event = new FundraisingEvent("Charity Run", Currency.USD);
        event.setId(eventId);

        BoxDto expectedDto = new BoxDto(boxId, "BOX-001", true, true);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(fundraisingEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(boxCurrencyRepository.isBoxEmpty(box)).thenReturn(true);
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.assignBoxToEvent(boxId, eventId);

        // Then
        assertNotNull(result);
        assertTrue(result.isAssigned());
        assertEquals(BoxStatus.ASSIGNED, box.getStatus());
        assertEquals(event, box.getAssignedEvent());

        verify(boxRepository).findById(boxId);
        verify(fundraisingEventRepository).findById(eventId);
        verify(boxCurrencyRepository).isBoxEmpty(box);
        verify(boxRepository).save(box);
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldThrowExceptionWhenAssigningAlreadyAssignedBox() {
        // Given
        Long boxId = 1L;
        Long eventId = 1L;
        Box box = new Box("BOX-001");
        box.setStatus(BoxStatus.ASSIGNED);

        FundraisingEvent event = new FundraisingEvent("Test Event", Currency.USD);
        event.setId(eventId);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(fundraisingEventRepository.findById(eventId)).thenReturn(Optional.of(event));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.assignBoxToEvent(boxId, eventId)
        );

        assertEquals("Box is already assigned to another event", exception.getMessage());
        verify(boxRepository).findById(boxId);
        verify(fundraisingEventRepository).findById(eventId);
    }

    @Test
    void shouldThrowExceptionWhenAssigningNonEmptyBox() {
        // Given
        Long boxId = 1L;
        Long eventId = 1L;
        Box box = new Box("BOX-001");
        box.setStatus(BoxStatus.AVAILABLE);
        FundraisingEvent event = new FundraisingEvent("Charity Run", Currency.USD);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(fundraisingEventRepository.findById(eventId)).thenReturn(Optional.of(event));
        when(boxCurrencyRepository.isBoxEmpty(box)).thenReturn(false);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.assignBoxToEvent(boxId, eventId)
        );

        assertEquals("Box must be empty before assignment", exception.getMessage());
    }

    @Test
    void shouldUnassignBoxFromEventSuccessfully() {
        // Given
        Long boxId = 1L;
        Box box = new Box("BOX-001");
        box.setStatus(BoxStatus.ASSIGNED);
        FundraisingEvent event = new FundraisingEvent("Charity Run", Currency.USD);
        box.setAssignedEvent(event);

        BoxDto expectedDto = new BoxDto(boxId, "BOX-001", false, true);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxRepository.save(box)).thenReturn(box);
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.unassignBoxFromEvent(boxId);

        // Then
        assertNotNull(result);
        assertFalse(result.isAssigned());
        assertEquals(BoxStatus.AVAILABLE, box.getStatus());
        assertNull(box.getAssignedEvent());

        verify(boxRepository).findById(boxId);
        verify(boxRepository).save(box);
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldAddMoneyToBoxSuccessfully() {
        // Given
        Long boxId = 1L;
        AddMoneyRequest request = new AddMoneyRequest(Currency.USD, new BigDecimal("50.00"));

        Box box = new Box("BOX-001");
        box.setId(boxId);
        box.setStatus(BoxStatus.ASSIGNED);

        BoxCurrency existingCurrency = new BoxCurrency(box, Currency.USD, new BigDecimal("25.00"));
        BoxDto expectedDto = new BoxDto(boxId, "BOX-001", true, false);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxCurrencyRepository.findByBoxAndCurrency(box, Currency.USD))
                .thenReturn(Optional.of(existingCurrency));
        when(boxCurrencyRepository.save(existingCurrency)).thenReturn(existingCurrency);
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.addMoneyToBox(boxId, request);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("75.00"), existingCurrency.getAmount());

        verify(boxRepository).findById(boxId);
        verify(boxCurrencyRepository).findByBoxAndCurrency(box, Currency.USD);
        verify(boxCurrencyRepository).save(existingCurrency);
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldAddMoneyToBoxWithNewCurrency() {
        // Given
        Long boxId = 1L;
        AddMoneyRequest request = new AddMoneyRequest(Currency.EUR, new BigDecimal("30.00"));

        Box box = new Box("BOX-001");
        box.setId(boxId);
        box.setStatus(BoxStatus.ASSIGNED);

        BoxDto expectedDto = new BoxDto(boxId, "BOX-001", true, false);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxCurrencyRepository.findByBoxAndCurrency(box, Currency.EUR))
                .thenReturn(Optional.empty());
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.addMoneyToBox(boxId, request);

        // Then
        assertNotNull(result);
        verify(boxRepository).findById(boxId);
        verify(boxCurrencyRepository).findByBoxAndCurrency(box, Currency.EUR);
        verify(boxCurrencyRepository).save(any(BoxCurrency.class));
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldThrowExceptionWhenAddingMoneyToUnassignedBox() {
        // Given
        Long boxId = 1L;
        AddMoneyRequest request = new AddMoneyRequest(Currency.USD, new BigDecimal("50.00"));

        Box box = new Box("BOX-001");
        box.setStatus(BoxStatus.AVAILABLE);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.addMoneyToBox(boxId, request)
        );

        assertEquals("Box must be assigned to a fundraising event before adding money", exception.getMessage());
    }

    @Test
    void shouldEmptyBoxSuccessfully() {
        // Given
        Long boxId = 1L;
        Box box = new Box("BOX-001");
        box.setId(boxId);
        box.setStatus(BoxStatus.ASSIGNED);

        FundraisingEvent event = new FundraisingEvent("Charity Run", Currency.USD);
        event.setBalance(new BigDecimal("100.00"));
        box.setAssignedEvent(event);

        List<BoxCurrency> boxCurrencies = Arrays.asList(
                new BoxCurrency(box, Currency.USD, new BigDecimal("50.00")),
                new BoxCurrency(box, Currency.EUR, new BigDecimal("25.00"))
        );

        BoxDto expectedDto = new BoxDto(boxId, "BOX-001", true, true);

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxCurrencyRepository.findByBox(box)).thenReturn(boxCurrencies);
        when(currencyConverter.convert(new BigDecimal("50.00"), Currency.USD, Currency.USD))
                .thenReturn(new BigDecimal("50.00"));
        when(currencyConverter.convert(new BigDecimal("25.00"), Currency.EUR, Currency.USD))
                .thenReturn(new BigDecimal("29.50"));
        when(fundraisingEventRepository.save(event)).thenReturn(event);
        when(boxMapper.toDto(box)).thenReturn(expectedDto);

        // When
        BoxDto result = service.emptyBox(boxId);

        // Then
        assertNotNull(result);
        assertEquals(new BigDecimal("179.50"), event.getBalance());

        verify(boxRepository).findById(boxId);
        verify(boxCurrencyRepository).findByBox(box);
        verify(currencyConverter).convert(new BigDecimal("50.00"), Currency.USD, Currency.USD);
        verify(currencyConverter).convert(new BigDecimal("25.00"), Currency.EUR, Currency.USD);
        verify(fundraisingEventRepository).save(event);
        verify(boxCurrencyRepository).deleteAll(boxCurrencies);
        verify(boxMapper).toDto(box);
    }

    @Test
    void shouldThrowExceptionWhenEmptyingAlreadyEmptyBox() {
        // Given
        Long boxId = 1L;
        Box box = new Box("BOX-001");
        box.setStatus(BoxStatus.ASSIGNED);
        FundraisingEvent event = new FundraisingEvent("Charity Run", Currency.USD);
        box.setAssignedEvent(event);

        List<BoxCurrency> emptyBoxCurrencies = Arrays.asList(
                new BoxCurrency(box, Currency.USD, BigDecimal.ZERO)
        );

        when(boxRepository.findById(boxId)).thenReturn(Optional.of(box));
        when(boxCurrencyRepository.findByBox(box)).thenReturn(emptyBoxCurrencies);

        // When & Then
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> service.emptyBox(boxId)
        );

        assertEquals("Box is already empty", exception.getMessage());
        verify(boxRepository).findById(boxId);
        verify(boxCurrencyRepository).findByBox(box);
        verify(fundraisingEventRepository, never()).save(any());
    }

    @Test
    void shouldGetAllBoxes() {
        // Given
        List<Box> boxes = Arrays.asList(
                new Box("BOX-001"),
                new Box("BOX-002")
        );

        List<BoxDto> expectedDtos = Arrays.asList(
                new BoxDto(1L, "BOX-001", false, true),
                new BoxDto(2L, "BOX-002", true, false)
        );

        when(boxRepository.findAll()).thenReturn(boxes);
        when(boxMapper.toDtoList(boxes)).thenReturn(expectedDtos);

        // When
        List<BoxDto> result = service.getAllBoxes();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("BOX-001", result.get(0).getBoxIdentifier());
        assertEquals("BOX-002", result.get(1).getBoxIdentifier());

        verify(boxRepository).findAll();
        verify(boxMapper).toDtoList(boxes);
    }
}