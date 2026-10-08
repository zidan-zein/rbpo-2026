package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.zidan.rbpo.entity.Exhibit;
import ru.zidan.rbpo.entity.ExhibitPlacement;
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

    public List<ExhibitPlacement> getAll() {
        return placementRepository.findAll();
    }

    public ExhibitPlacement getById(Long id) {
        return placementRepository.findById(id);
    }

    public HttpStatus checkCreate(ExhibitPlacement data) {
        if (data.getExhibitId() == null || data.getExhibitionId() == null || data.getHallId() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        return check(data.getExhibitId(), data.getExhibitionId(), data.getHallId(), null);
    }

    public HttpStatus checkUpdate(Long id, ExhibitPlacement data) {
        ExhibitPlacement p = placementRepository.findById(id);

        Long exhibitId = p.getExhibitId();
        Long exhibitionId = p.getExhibitionId();
        Long hallId = p.getHallId();
        boolean changed = false;
        if (data.getExhibitId() != null) {
            exhibitId = data.getExhibitId();
            changed = true;
        }
        if (data.getExhibitionId() != null) {
            exhibitionId = data.getExhibitionId();
            changed = true;
        }
        if (data.getHallId() != null) {
            hallId = data.getHallId();
            changed = true;
        }

        // проверяю заново только если поменяли экспонат, выставку или зал,
        // если меняют только статус монтажа то проверять нечего
        if (changed) {
            return check(exhibitId, exhibitionId, hallId, id);
        }
        return null;
    }

    public ExhibitPlacement create(ExhibitPlacement data) {
        ExhibitPlacement p = new ExhibitPlacement();
        p.setExhibitId(data.getExhibitId());
        p.setExhibitionId(data.getExhibitionId());
        p.setHallId(data.getHallId());
        if (data.getMountingStatus() == null) {
            p.setMountingStatus("PLANNED");
        } else {
            p.setMountingStatus(data.getMountingStatus());
        }
        p.setCreatedAt(LocalDateTime.now());
        return placementRepository.add(p);
    }

    public ExhibitPlacement update(Long id, ExhibitPlacement data) {
        ExhibitPlacement p = placementRepository.findById(id);

        if (data.getExhibitId() != null) {
            p.setExhibitId(data.getExhibitId());
        }
        if (data.getExhibitionId() != null) {
            p.setExhibitionId(data.getExhibitionId());
        }
        if (data.getHallId() != null) {
            p.setHallId(data.getHallId());
        }
        if (data.getMountingStatus() != null) {
            p.setMountingStatus(data.getMountingStatus());
        }
        return p;
    }

    public void delete(Long id) {
        placementRepository.deleteById(id);
    }

    // проверки перед тем как поставить экспонат в зал.
    // placementId - это id размещения которое меняем (при создании null),
    // его не считаю, иначе экспонат посчитается два раза
    private HttpStatus check(Long exhibitId, Long exhibitionId, Long hallId, Long placementId) {
        Exhibit exhibit = exhibitRepository.findById(exhibitId);
        if (exhibit == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (exhibitionRepository.findById(exhibitionId) == null) {
            return HttpStatus.BAD_REQUEST;
        }
        ExhibitionHall hall = hallRepository.findById(hallId);
        if (hall == null) {
            return HttpStatus.BAD_REQUEST;
        }
        // зал закрыт (например ремонт)
        if (!hall.getAvailable()) {
            return HttpStatus.CONFLICT;
        }

        // считаю сколько места в зале уже занято экспонатами этой же выставки
        double used = 0;
        List<ExhibitPlacement> list = placementRepository.findByHallId(hallId);
        for (ExhibitPlacement other : list) {
            if (other.getId().equals(placementId)) {
                continue;
            }
            if (other.getExhibitionId().equals(exhibitionId)) {
                Exhibit e = exhibitRepository.findById(other.getExhibitId());
                used = used + e.getRequiredArea();
            }
        }

        double free = hall.getArea() - used;
        if (exhibit.getRequiredArea() > free) {
            return HttpStatus.CONFLICT; // места не хватает
        }
        return null;
    }
}
