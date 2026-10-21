package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.entity.ExhibitionHall;
import ru.zidan.rbpo.repository.ExhibitPlacementRepository;
import ru.zidan.rbpo.repository.ExhibitionHallRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionHallService {

    private final ExhibitionHallRepository hallRepository;
    private final ExhibitPlacementRepository placementRepository;

    public List<ExhibitionHall> getAll() {
        return hallRepository.findAll();
    }

    public ExhibitionHall getById(Long id) {
        return hallRepository.findById(id).orElse(null);
    }

    public HttpStatus checkCreate(ExhibitionHall data) {
        if (data.getName() == null || data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getArea() == null || data.getArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        if (hallRepository.findByName(data.getName()) != null) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    public HttpStatus checkUpdate(Long id, ExhibitionHall data) {
        if (data.getName() != null) {
            if (data.getName().isBlank()) {
                return HttpStatus.BAD_REQUEST;
            }
            ExhibitionHall other = hallRepository.findByName(data.getName());
            if (other != null && !other.getId().equals(id)) {
                return HttpStatus.CONFLICT;
            }
        }
        if (data.getArea() != null && data.getArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        // закрыть зал нельзя, пока в нем идет открытая выставка
        if (data.getAvailable() != null && !data.getAvailable() && usedByOpenExhibition(id)) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    public HttpStatus checkDelete(Long id) {
        if (usedByOpenExhibition(id)) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    public ExhibitionHall create(ExhibitionHall data) {
        ExhibitionHall hall = new ExhibitionHall();
        hall.setName(data.getName());
        hall.setArea(data.getArea());
        // если доступность не прислали, считаю что зал доступен
        if (data.getAvailable() == null) {
            hall.setAvailable(true);
        } else {
            hall.setAvailable(data.getAvailable());
        }
        hall.setCreatedAt(LocalDateTime.now());
        return hallRepository.save(hall);
    }

    public ExhibitionHall update(Long id, ExhibitionHall data) {
        ExhibitionHall hall = hallRepository.findById(id).orElse(null);

        if (data.getName() != null) {
            hall.setName(data.getName());
        }
        if (data.getArea() != null) {
            hall.setArea(data.getArea());
        }
        if (data.getAvailable() != null) {
            hall.setAvailable(data.getAvailable());
        }
        return hallRepository.save(hall);
    }

    @Transactional
    public void delete(Long id) {
        // размещения в этом зале удалит база (ON DELETE CASCADE)
        hallRepository.deleteById(id);
    }

    private boolean usedByOpenExhibition(Long hallId) {
        List<ExhibitPlacement> list = placementRepository.findByHallId(hallId);
        for (ExhibitPlacement p : list) {
            if (p.getExhibition().getStatus().equals("OPEN")) {
                return true;
            }
        }
        return false;
    }
}
