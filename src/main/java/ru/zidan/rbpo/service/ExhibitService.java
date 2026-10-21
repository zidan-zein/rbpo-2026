package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
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
        return exhibitRepository.findById(id).orElse(null);
    }

    // проверка перед созданием: null - все хорошо, иначе код ошибки
    public HttpStatus checkCreate(Exhibit data) {
        if (data.getName() == null || data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getRequiredArea() == null || data.getRequiredArea() <= 0) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getCondition() != null && !conditionAllowed(data.getCondition())) {
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
        if (data.getCondition() != null && !conditionAllowed(data.getCondition())) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    // экспонат с открытой выставки удалять нельзя
    public HttpStatus checkDelete(Long id) {
        if (onOpenExhibition(id)) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    public Exhibit create(Exhibit data) {
        Exhibit exhibit = new Exhibit();
        exhibit.setName(data.getName());
        if (data.getCondition() == null) {
            exhibit.setCondition("GOOD");
        } else {
            exhibit.setCondition(data.getCondition());
        }
        exhibit.setRequiredArea(data.getRequiredArea());
        exhibit.setCreatedAt(LocalDateTime.now()); // время ставит сервер
        return exhibitRepository.save(exhibit);
    }

    public Exhibit update(Long id, Exhibit data) {
        Exhibit exhibit = exhibitRepository.findById(id).orElse(null);

        if (data.getName() != null) {
            exhibit.setName(data.getName());
        }
        if (data.getCondition() != null) {
            exhibit.setCondition(data.getCondition());
        }
        if (data.getRequiredArea() != null) {
            exhibit.setRequiredArea(data.getRequiredArea());
        }
        return exhibitRepository.save(exhibit);
    }

    @Transactional
    public void delete(Long id) {
        // размещения этого экспоната удалит база (ON DELETE CASCADE)
        exhibitRepository.deleteById(id);
    }

    // бизнес-операция: отправить экспонат на реставрацию.
    // с открытой выставки снять нельзя, а из запланированных выставок он убирается
    @Transactional
    public Exhibit sendToRestoration(Long id) {
        Exhibit exhibit = exhibitRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Экспонат не найден"));

        if (exhibit.getCondition().equals("IN_RESTORATION")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Экспонат уже на реставрации");
        }
        if (onOpenExhibition(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Экспонат сейчас на открытой выставке, сначала закройте ее");
        }

        List<ExhibitPlacement> list = placementRepository.findByExhibitId(id);
        for (ExhibitPlacement p : list) {
            // закрытые выставки не трогаю, это уже история
            if (p.getExhibition().getStatus().equals("PLANNED")) {
                placementRepository.delete(p);
            }
        }
        exhibit.setCondition("IN_RESTORATION");
        return exhibitRepository.save(exhibit);
    }

    // IN_RESTORATION руками ставить нельзя, только через операцию выше
    private boolean conditionAllowed(String condition) {
        return condition.equals("GOOD") || condition.equals("NEEDS_RESTORATION");
    }

    private boolean onOpenExhibition(Long exhibitId) {
        List<ExhibitPlacement> list = placementRepository.findByExhibitId(exhibitId);
        for (ExhibitPlacement p : list) {
            if (p.getExhibition().getStatus().equals("OPEN")) {
                return true;
            }
        }
        return false;
    }
}
