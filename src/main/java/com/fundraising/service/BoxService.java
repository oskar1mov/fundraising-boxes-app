package com.fundraising.service;

import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.entity.Box;
import com.fundraising.exception.BoxNotFoundException;
import com.fundraising.exception.DuplicateBoxIdentifierException;
import com.fundraising.mapper.BoxMapper;
import com.fundraising.repository.BoxCurrencyRepository;
import com.fundraising.repository.BoxRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BoxService {

    private final BoxRepository boxRepository;
    private final BoxCurrencyRepository boxCurrencyRepository;
    private final BoxMapper boxMapper;

    public BoxService(BoxRepository boxRepository, BoxCurrencyRepository boxCurrencyRepository, BoxMapper boxMapper) {
        this.boxRepository = boxRepository;
        this.boxCurrencyRepository = boxCurrencyRepository;
        this.boxMapper = boxMapper;
    }

    public BoxDto registerBox(CreateBoxRequest request) {
        // Check for duplicate box identifier
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

        // Delete the box itself
        boxRepository.delete(box);
    }

    @Transactional(readOnly = true)
    public List<BoxDto> getAllBoxes() {
        List<Box> boxes = boxRepository.findAll();
        return boxMapper.toDtoList(boxes);
    }
}