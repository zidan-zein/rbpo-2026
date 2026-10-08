package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
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
        return hallRepository.findById(id);
    }

    public HttpStatus checkCreate(ExhibitionHall data) {
        if (data.getArea() == null || data.getArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    public HttpStatus checkUpdate(ExhibitionHall data) {
        if (data.getArea() != null && data.getArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
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
        return hallRepository.add(hall);
    }

    public ExhibitionHall update(Long id, ExhibitionHall data) {
        ExhibitionHall hall = hallRepository.findById(id);

        if (data.getName() != null) {
            hall.setName(data.getName());
        }
        if (data.getArea() != null) {
            hall.setArea(data.getArea());
        }
        if (data.getAvailable() != null) {
            hall.setAvailable(data.getAvailable());
        }
        return hall;
    }

    public void delete(Long id) {
        List<ExhibitPlacement> list = placementRepository.findByHallId(id);
        for (ExhibitPlacement p : list) {
            placementRepository.deleteById(p.getId());
        }
        hallRepository.deleteById(id);
    }
}
