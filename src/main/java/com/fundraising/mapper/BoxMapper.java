package com.fundraising.mapper;

import com.fundraising.dto.BoxDto;
import com.fundraising.dto.CreateBoxRequest;
import com.fundraising.entity.Box;
import com.fundraising.enums.BoxStatus;
import com.fundraising.repository.BoxCurrencyRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class BoxMapper {

    private final BoxCurrencyRepository boxCurrencyRepository;

    public BoxMapper(BoxCurrencyRepository boxCurrencyRepository) {
        this.boxCurrencyRepository = boxCurrencyRepository;
    }

    public Box toEntity(CreateBoxRequest request) {
        return new Box(request.getBoxIdentifier());
    }

    public BoxDto toDto(Box entity) {
        boolean isEmpty = boxCurrencyRepository.isBoxEmpty(entity);
        return new BoxDto(
                entity.getId(),
                entity.getBoxIdentifier(),
                entity.getStatus() == BoxStatus.ASSIGNED,
                isEmpty
        );
    }

    public List<BoxDto> toDtoList(List<Box> entities) {
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}
