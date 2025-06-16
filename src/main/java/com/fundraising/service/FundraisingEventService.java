package com.fundraising.service;

import com.fundraising.dto.CreateEventRequest;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.entity.FundraisingEvent;
import com.fundraising.exception.DuplicateEventNameException;
import com.fundraising.mapper.FundraisingEventMapper;
import com.fundraising.repository.FundraisingEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class FundraisingEventService {

    @Autowired
    private FundraisingEventRepository fundraisingEventRepository;

    @Autowired
    private FundraisingEventMapper mapper;

    public FundraisingEventDTO createEvent(CreateEventRequest request) {
        if (fundraisingEventRepository.existsByNameIgnoreCase(request.getName())) {
            throw new DuplicateEventNameException(
                    "An event with the name '" + request.getName() + "' already exists");
        }

        FundraisingEvent entity = mapper.toEntity(request);
        FundraisingEvent savedEntity = fundraisingEventRepository.save(entity);
        return mapper.toDto(savedEntity);
    }

    public List<FundraisingEventDTO> getAllEvents() {
        List<FundraisingEvent> entities = fundraisingEventRepository.findAll();
        return mapper.toDtoList(entities);
    }

    public Optional<FundraisingEventDTO> getEventById(Long id) {
        return fundraisingEventRepository.findById(id)
                .map(mapper::toDto);
    }
}