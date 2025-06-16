package com.fundraising.mapper;

import com.fundraising.dto.CreateEventRequest;
import com.fundraising.dto.FundraisingEventDTO;
import com.fundraising.entity.FundraisingEvent;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class FundraisingEventMapper {

    public FundraisingEvent toEntity(CreateEventRequest request) {
        return new FundraisingEvent(request.getName(), request.getCurrency());
    }

    public FundraisingEventDTO toDto(FundraisingEvent entity) {
        return new FundraisingEventDTO(
                entity.getId(),
                entity.getName(),
                entity.getBalance(),
                entity.getCurrency()
        );
    }

    public List<FundraisingEventDTO> toDtoList(List<FundraisingEvent> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}