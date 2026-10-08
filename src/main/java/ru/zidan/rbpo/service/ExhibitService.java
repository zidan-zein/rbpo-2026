package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.zidan.rbpo.entity.Exhibit;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.repository.ExhibitPlacementRepository;
import ru.zidan.rbpo.repository.ExhibitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitService {

    private final ExhibitRepository exhibitRepository;
    private final ExhibitPlacementRepository placementRepository;

    public List<Exhibit> getAll() {
        return exhibitRepository.findAll();
    }

    // если такого нет, вернется null
    public Exhibit getById(Long id) {
        return exhibitRepository.findById(id);
    }

    // проверка перед созданием: null - все хорошо, иначе код ошибки
    public HttpStatus checkCreate(Exhibit data) {
        if (data.getName() == null || data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getRequiredArea() == null || data.getRequiredArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    // при PATCH поля могут не прислать, проверяю только те что пришли
    public HttpStatus checkUpdate(Exhibit data) {
        if (data.getName() != null && data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getRequiredArea() != null && data.getRequiredArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    public Exhibit create(Exhibit data) {
        Exhibit exhibit = new Exhibit();
        exhibit.setName(data.getName());
        exhibit.setCondition(data.getCondition());
        exhibit.setRequiredArea(data.getRequiredArea());
        exhibit.setCreatedAt(LocalDateTime.now()); // время ставит сервер
        return exhibitRepository.add(exhibit);
    }

    public Exhibit update(Long id, Exhibit data) {
        Exhibit exhibit = exhibitRepository.findById(id);

        if (data.getName() != null) {
            exhibit.setName(data.getName());
        }
        if (data.getCondition() != null) {
            exhibit.setCondition(data.getCondition());
        }
        if (data.getRequiredArea() != null) {
            exhibit.setRequiredArea(data.getRequiredArea());
        }
        return exhibit;
    }

    public void delete(Long id) {
        // размещение без экспоната не имеет смысла, поэтому удаляю и его
        List<ExhibitPlacement> list = placementRepository.findByExhibitId(id);
        for (ExhibitPlacement p : list) {
            placementRepository.deleteById(p.getId());
        }
        exhibitRepository.deleteById(id);
    }
}
