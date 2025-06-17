package com.fundraising.service;

import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.entity.Box;
import com.fundraising.entity.FundraisingEvent;
import com.fundraising.enums.BoxStatus;
import com.fundraising.exception.BoxNotFoundException;
import com.fundraising.exception.DuplicateBoxIdentifierException;
import com.fundraising.mapper.BoxMapper;
import com.fundraising.repository.BoxCurrencyRepository;
import com.fundraising.repository.BoxRepository;
import com.fundraising.repository.FundraisingEventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BoxService {

    private final BoxRepository boxRepository;
    private final BoxCurrencyRepository boxCurrencyRepository;
    private final FundraisingEventRepository fundraisingEventRepository;
    private final BoxMapper boxMapper;

    public BoxService(BoxRepository boxRepository, BoxCurrencyRepository boxCurrencyRepository,
                      FundraisingEventRepository fundraisingEventRepository, BoxMapper boxMapper) {
        this.boxRepository = boxRepository;
        this.boxCurrencyRepository = boxCurrencyRepository;
        this.fundraisingEventRepository = fundraisingEventRepository;
        this.boxMapper = boxMapper;
    }

    public BoxDto registerBox(CreateBoxRequest request) {
        if (boxRepository.existsByBoxIdentifier(request.getBoxIdentifier())) {
            throw new DuplicateBoxIdentifierException("Box with identifier '" + request.getBoxIdentifier() + "' already exists");
        }

        Box box = boxMapper.toEntity(request);
        Box savedBox = boxRepository.save(box);
        return boxMapper.toDto(savedBox);
    }

    public void unregisterBox(Long id) {
        Box box = boxRepository.findById(id)
                .orElseThrow(() -> new BoxNotFoundException("Box with ID " + id + " not found"));

        // When a box is unregistered, it's automatically emptied (money is not transferred)
        // Delete all currency records for this box
        List<com.fundraising.entity.BoxCurrency> currencies = boxCurrencyRepository.findByBox(box);
        boxCurrencyRepository.deleteAll(currencies);

        boxRepository.delete(box);
    }

    public BoxDto assignBoxToEvent(Long boxId, Long eventId) {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxNotFoundException("Box with ID " + boxId + " not found"));

        FundraisingEvent event = fundraisingEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException("Fundraising event with ID " + eventId + " not found"));

        if (box.getStatus() != BoxStatus.AVAILABLE) {
            throw new IllegalStateException("Box is already assigned to another event");
        }

        boolean isEmpty = boxCurrencyRepository.isBoxEmpty(box);
        if (!isEmpty) {
            throw new IllegalStateException("Box must be empty before assignment");
        }

        box.setStatus(BoxStatus.ASSIGNED);
        box.setAssignedEvent(event);
        Box savedBox = boxRepository.save(box);

        return boxMapper.toDto(savedBox);
    }

    public BoxDto unassignBoxFromEvent(Long boxId) {
        Box box = boxRepository.findById(boxId)
                .orElseThrow(() -> new BoxNotFoundException("Box with ID " + boxId + " not found"));

        if (box.getStatus() != BoxStatus.ASSIGNED) {
            throw new IllegalStateException("Box is not currently assigned to any event");
        }

        box.setStatus(BoxStatus.AVAILABLE);
        box.setAssignedEvent(null);
        Box savedBox = boxRepository.save(box);

        return boxMapper.toDto(savedBox);
    }

    public List<BoxDto> getAllBoxes() {
        List<Box> boxes = boxRepository.findAll();
        return boxMapper.toDtoList(boxes);
    }
}