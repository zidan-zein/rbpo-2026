package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.entity.Exhibition;
import ru.zidan.rbpo.repository.ExhibitPlacementRepository;
import ru.zidan.rbpo.repository.ExhibitionRepository;
import ru.zidan.rbpo.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    private final ExhibitionRepository exhibitionRepository;
    private final UserRepository userRepository;
    private final ExhibitPlacementRepository placementRepository;

    public List<Exhibition> getAll() {
        return exhibitionRepository.findAll();
    }

    public Exhibition getById(Long id) {
        return exhibitionRepository.findById(id);
    }

    public HttpStatus checkCreate(Exhibition data) {
        if (data.getName() == null || data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getStartDate() == null || data.getEndDate() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getEndDate().isBefore(data.getStartDate())) {
            return HttpStatus.BAD_REQUEST;
        }
        if (!curatorExists(data.getCuratorId())) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    public HttpStatus checkUpdate(Long id, Exhibition data) {
        Exhibition ex = exhibitionRepository.findById(id);

        if (data.getName() != null && data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }

        // даты проверяю вместе: могли прислать только одну из них
        LocalDate start = ex.getStartDate();
        LocalDate end = ex.getEndDate();
        if (data.getStartDate() != null) {
            start = data.getStartDate();
        }
        if (data.getEndDate() != null) {
            end = data.getEndDate();
        }
        if (end.isBefore(start)) {
            return HttpStatus.BAD_REQUEST;
        }

        if (!curatorExists(data.getCuratorId())) {
            return HttpStatus.BAD_REQUEST;
        }
        return null;
    }

    public Exhibition create(Exhibition data) {
        Exhibition ex = new Exhibition();
        ex.setName(data.getName());
        ex.setStartDate(data.getStartDate());
        ex.setEndDate(data.getEndDate());
        if (data.getStatus() == null) {
            ex.setStatus("PLANNED"); // по умолчанию выставка только планируется
        } else {
            ex.setStatus(data.getStatus());
        }
        ex.setCuratorId(data.getCuratorId());
        ex.setCreatedAt(LocalDateTime.now());
        return exhibitionRepository.add(ex);
    }

    public Exhibition update(Long id, Exhibition data) {
        Exhibition ex = exhibitionRepository.findById(id);

        if (data.getName() != null) {
            ex.setName(data.getName());
        }
        if (data.getStartDate() != null) {
            ex.setStartDate(data.getStartDate());
        }
        if (data.getEndDate() != null) {
            ex.setEndDate(data.getEndDate());
        }
        if (data.getStatus() != null) {
            ex.setStatus(data.getStatus());
        }
        if (data.getCuratorId() != null) {
            ex.setCuratorId(data.getCuratorId());
        }
        return ex;
    }

    public void delete(Long id) {
        // без выставки размещения тоже не нужны
        List<ExhibitPlacement> list = placementRepository.findByExhibitionId(id);
        for (ExhibitPlacement p : list) {
            placementRepository.deleteById(p.getId());
        }
        exhibitionRepository.deleteById(id);
    }

    // куратор необязательный, но если его указали, то такой пользователь должен быть
    private boolean curatorExists(Long curatorId) {
        if (curatorId == null) {
            return true;
        }
        return userRepository.findById(curatorId) != null;
    }
}
