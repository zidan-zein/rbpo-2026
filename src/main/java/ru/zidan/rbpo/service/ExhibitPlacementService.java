package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zidan.rbpo.dto.ExhibitPlacementDto;
import ru.zidan.rbpo.entity.Exhibit;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.entity.Exhibition;
import ru.zidan.rbpo.entity.ExhibitionHall;
import ru.zidan.rbpo.repository.ExhibitPlacementRepository;
import ru.zidan.rbpo.repository.ExhibitRepository;
import ru.zidan.rbpo.repository.ExhibitionHallRepository;
import ru.zidan.rbpo.repository.ExhibitionRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitPlacementService {

    private final ExhibitPlacementRepository placementRepository;
    private final ExhibitRepository exhibitRepository;
    private final ExhibitionRepository exhibitionRepository;
    private final ExhibitionHallRepository hallRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<ExhibitPlacementDto> getAll() {
        return mapper.toPlacementDtos(placementRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ExhibitPlacementDto getById(Long id) {
        ExhibitPlacement p = placementRepository.findById(id).orElse(null);
        if (p == null) {
            return null;
        }
        return mapper.toDto(p);
    }

    public HttpStatus checkCreate(ExhibitPlacementDto data) {
        if (data.getExhibitId() == null || data.getExhibitionId() == null || data.getHallId() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        Exhibit exhibit = exhibitRepository.findById(data.getExhibitId()).orElse(null);
        Exhibition exhibition = exhibitionRepository.findById(data.getExhibitionId()).orElse(null);
        ExhibitionHall hall = hallRepository.findById(data.getHallId()).orElse(null);
        if (exhibit == null || exhibition == null || hall == null) {
            return HttpStatus.BAD_REQUEST;
        }

        // состав можно менять только пока выставка запланирована
        if (!exhibition.getStatus().equals("PLANNED")) {
            return HttpStatus.CONFLICT;
        }
        if (exhibit.getCondition().equals("IN_RESTORATION")) {
            return HttpStatus.CONFLICT;
        }
        if (busyInSameTime(exhibit, exhibition)) {
            return HttpStatus.CONFLICT;
        }
        return checkHall(exhibit, exhibition, hall, null);
    }

    // в размещении можно поменять только зал
    public HttpStatus checkUpdate(Long id, ExhibitPlacementDto data) {
        if (data.getExhibitId() != null || data.getExhibitionId() != null || data.getMountingStatus() != null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getHallId() == null) {
            return null; // менять нечего
        }
        ExhibitPlacement p = placementRepository.findById(id).orElse(null);
        if (!p.getExhibition().getStatus().equals("PLANNED")) {
            return HttpStatus.CONFLICT;
        }
        ExhibitionHall hall = hallRepository.findById(data.getHallId()).orElse(null);
        if (hall == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return checkHall(p.getExhibit(), p.getExhibition(), hall, id);
    }

    // с открытой выставки экспонат просто так не убрать
    public HttpStatus checkDelete(Long id) {
        ExhibitPlacement p = placementRepository.findById(id).orElse(null);
        if (p.getExhibition().getStatus().equals("OPEN")) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    @Transactional
    public ExhibitPlacementDto create(ExhibitPlacementDto data) {
        ExhibitPlacement p = new ExhibitPlacement();
        p.setExhibit(exhibitRepository.findById(data.getExhibitId()).orElse(null));
        p.setExhibition(exhibitionRepository.findById(data.getExhibitionId()).orElse(null));
        p.setHall(hallRepository.findById(data.getHallId()).orElse(null));
        p.setMountingStatus("PLANNED");
        p.setCreatedAt(LocalDateTime.now());
        return mapper.toDto(placementRepository.save(p));
    }

    @Transactional
    public ExhibitPlacementDto update(Long id, ExhibitPlacementDto data) {
        ExhibitPlacement p = placementRepository.findById(id).orElse(null);
        if (data.getHallId() != null) {
            p.setHall(hallRepository.findById(data.getHallId()).orElse(null));
        }
        return mapper.toDto(placementRepository.save(p));
    }

    public void delete(Long id) {
        placementRepository.deleteById(id);
    }

    // экспонат не может быть сразу на двух активных выставках, если их даты пересекаются
    private boolean busyInSameTime(Exhibit exhibit, Exhibition target) {
        List<ExhibitPlacement> list = placementRepository.findByExhibitId(exhibit.getId());
        for (ExhibitPlacement other : list) {
            Exhibition ex = other.getExhibition();
            if (ex.getId().equals(target.getId())) {
                return true; // уже есть на этой выставке
            }
            boolean active = ex.getStatus().equals("PLANNED") || ex.getStatus().equals("OPEN");
            boolean datesCross = !ex.getEndDate().isBefore(target.getStartDate())
                    && !ex.getStartDate().isAfter(target.getEndDate());
            if (active && datesCross) {
                return true;
            }
        }
        return false;
    }

    // зал должен быть открыт и в нем должно хватить места.
    // placementId - это id размещения которое меняем (при создании null),
    // его не считаю, иначе экспонат посчитается два раза
    private HttpStatus checkHall(Exhibit exhibit, Exhibition exhibition, ExhibitionHall hall, Long placementId) {
        if (!hall.getAvailable()) {
            return HttpStatus.CONFLICT;
        }

        // считаю сколько места в зале уже занято экспонатами этой же выставки
        double used = 0;
        List<ExhibitPlacement> list = placementRepository.findByExhibitionIdAndHallId(exhibition.getId(), hall.getId());
        for (ExhibitPlacement other : list) {
            if (other.getId().equals(placementId)) {
                continue;
            }
            used = used + other.getExhibit().getRequiredArea();
        }

        double free = hall.getArea() - used;
        if (exhibit.getRequiredArea() > free) {
            return HttpStatus.CONFLICT; // места не хватает
        }
        return null;
    }
}
