package ru.zidan.rbpo.service;

import org.springframework.stereotype.Component;
import ru.zidan.rbpo.dto.ExhibitPlacementDto;
import ru.zidan.rbpo.dto.ExhibitionDto;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.entity.Exhibition;

import java.util.ArrayList;
import java.util.List;

// переделывает сущности со связями в dto, чтобы в json были просто id
@Component
public class DtoMapper {

    public ExhibitionDto toDto(Exhibition exhibition) {
        ExhibitionDto dto = new ExhibitionDto();
        dto.setId(exhibition.getId());
        dto.setName(exhibition.getName());
        dto.setStartDate(exhibition.getStartDate());
        dto.setEndDate(exhibition.getEndDate());
        dto.setStatus(exhibition.getStatus());
        if (exhibition.getCurator() != null) {
            dto.setCuratorId(exhibition.getCurator().getId());
        }
        dto.setCreatedAt(exhibition.getCreatedAt());
        return dto;
    }

    public ExhibitPlacementDto toDto(ExhibitPlacement placement) {
        ExhibitPlacementDto dto = new ExhibitPlacementDto();
        dto.setId(placement.getId());
        dto.setExhibitId(placement.getExhibit().getId());
        dto.setExhibitionId(placement.getExhibition().getId());
        dto.setHallId(placement.getHall().getId());
        dto.setMountingStatus(placement.getMountingStatus());
        dto.setCreatedAt(placement.getCreatedAt());
        return dto;
    }

    public List<ExhibitionDto> toExhibitionDtos(List<Exhibition> list) {
        List<ExhibitionDto> result = new ArrayList<>();
        for (Exhibition e : list) {
            result.add(toDto(e));
        }
        return result;
    }

    public List<ExhibitPlacementDto> toPlacementDtos(List<ExhibitPlacement> list) {
        List<ExhibitPlacementDto> result = new ArrayList<>();
        for (ExhibitPlacement p : list) {
            result.add(toDto(p));
        }
        return result;
    }

}
