package ru.zidan.rbpo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.zidan.rbpo.dto.ExhibitionDto;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.entity.Exhibition;
import ru.zidan.rbpo.entity.User;
import ru.zidan.rbpo.repository.ExhibitPlacementRepository;
import ru.zidan.rbpo.repository.ExhibitionRepository;
import ru.zidan.rbpo.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExhibitionService {

    // у одного куратора не больше двух активных выставок (запланированных или открытых)
    private static final int MAX_ACTIVE_FOR_CURATOR = 2;

    private final ExhibitionRepository exhibitionRepository;
    private final UserRepository userRepository;
    private final ExhibitPlacementRepository placementRepository;
    private final DtoMapper mapper;

    @Transactional(readOnly = true)
    public List<ExhibitionDto> getAll() {
        return mapper.toExhibitionDtos(exhibitionRepository.findAll());
    }

    @Transactional(readOnly = true)
    public ExhibitionDto getById(Long id) {
        Exhibition ex = exhibitionRepository.findById(id).orElse(null);
        if (ex == null) {
            return null;
        }
        return mapper.toDto(ex);
    }

    public HttpStatus checkCreate(ExhibitionDto data) {
        if (data.getName() == null || data.getName().isBlank()) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getStartDate() == null || data.getEndDate() == null) {
            return HttpStatus.BAD_REQUEST;
        }
        if (data.getEndDate().isBefore(data.getStartDate())) {
            return HttpStatus.BAD_REQUEST;
        }
        // куратор необязательный, но если его указали, то такой пользователь должен быть
        if (data.getCuratorId() != null) {
            if (!userRepository.existsById(data.getCuratorId())) {
                return HttpStatus.BAD_REQUEST;
            }
            if (curatorIsBusy(data.getCuratorId())) {
                return HttpStatus.CONFLICT;
            }
        }
        return null;
    }

    public HttpStatus checkUpdate(Long id, ExhibitionDto data) {
        Exhibition ex = exhibitionRepository.findById(id).orElse(null);

        // статус и куратор меняются только через бизнес-операции
        if (data.getStatus() != null || data.getCuratorId() != null) {
            return HttpStatus.BAD_REQUEST;
        }
        // закрытую или отмененную выставку уже не меняем
        if (ex.getStatus().equals("CLOSED") || ex.getStatus().equals("CANCELLED")) {
            return HttpStatus.CONFLICT;
        }
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
        return null;
    }

    // открытую выставку удалить нельзя, ее надо сначала закрыть
    public HttpStatus checkDelete(Long id) {
        Exhibition ex = exhibitionRepository.findById(id).orElse(null);
        if (ex.getStatus().equals("OPEN")) {
            return HttpStatus.CONFLICT;
        }
        return null;
    }

    @Transactional
    public ExhibitionDto create(ExhibitionDto data) {
        Exhibition ex = new Exhibition();
        ex.setName(data.getName());
        ex.setStartDate(data.getStartDate());
        ex.setEndDate(data.getEndDate());
        ex.setStatus("PLANNED"); // новая выставка всегда только планируется
        if (data.getCuratorId() != null) {
            ex.setCurator(userRepository.findById(data.getCuratorId()).orElse(null));
        }
        ex.setCreatedAt(LocalDateTime.now());
        return mapper.toDto(exhibitionRepository.save(ex));
    }

    @Transactional
    public ExhibitionDto update(Long id, ExhibitionDto data) {
        Exhibition ex = exhibitionRepository.findById(id).orElse(null);

        if (data.getName() != null) {
            ex.setName(data.getName());
        }
        if (data.getStartDate() != null) {
            ex.setStartDate(data.getStartDate());
        }
        if (data.getEndDate() != null) {
            ex.setEndDate(data.getEndDate());
        }
        return mapper.toDto(exhibitionRepository.save(ex));
    }

    @Transactional
    public void delete(Long id) {
        // размещения этой выставки удалит база (ON DELETE CASCADE)
        exhibitionRepository.deleteById(id);
    }

    // ниже бизнес-операции. каждая в одной транзакции:
    // если где-то ошибка (исключение), то все изменения откатятся

    // открыть выставку
    @Transactional
    public ExhibitionDto open(Long id) {
        Exhibition ex = requireExhibition(id);

        if (!ex.getStatus().equals("PLANNED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Открыть можно только запланированную выставку");
        }
        if (ex.getCurator() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "У выставки нет куратора");
        }
        if (ex.getEndDate().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Дата окончания выставки уже прошла");
        }

        List<ExhibitPlacement> list = placementRepository.findByExhibitionId(id);
        if (list.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "На выставке нет ни одного экспоната");
        }
        for (ExhibitPlacement p : list) {
            if (!p.getExhibit().getCondition().equals("GOOD")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Экспонат " + p.getExhibit().getId() + " не в хорошем состоянии");
            }
            if (!p.getHall().getAvailable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Зал " + p.getHall().getId() + " сейчас недоступен");
            }
        }

        // все проверки прошли: выставка открыта, экспонаты смонтированы
        for (ExhibitPlacement p : list) {
            p.setMountingStatus("MOUNTED");
        }
        ex.setStatus("OPEN");
        return mapper.toDto(ex);
    }

    // закрыть выставку
    @Transactional
    public ExhibitionDto close(Long id) {
        Exhibition ex = requireExhibition(id);

        if (!ex.getStatus().equals("OPEN")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Закрыть можно только открытую выставку");
        }

        List<ExhibitPlacement> list = placementRepository.findByExhibitionId(id);
        for (ExhibitPlacement p : list) {
            p.setMountingStatus("DISMANTLED");
        }
        ex.setStatus("CLOSED");
        return mapper.toDto(ex);
    }

    // отменить выставку, которая еще не открылась
    @Transactional
    public ExhibitionDto cancel(Long id) {
        Exhibition ex = requireExhibition(id);

        if (!ex.getStatus().equals("PLANNED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Отменить можно только запланированную выставку, открытую нужно закрыть");
        }

        // экспонаты освобождаются, их можно ставить на другие выставки
        List<ExhibitPlacement> list = placementRepository.findByExhibitionId(id);
        placementRepository.deleteAll(list);
        ex.setStatus("CANCELLED");
        return mapper.toDto(ex);
    }

    // назначить другого куратора
    @Transactional
    public ExhibitionDto changeCurator(Long id, Long userId) {
        Exhibition ex = requireExhibition(id);

        if (ex.getStatus().equals("CLOSED") || ex.getStatus().equals("CANCELLED")) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Выставка уже завершена");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Пользователь не найден"));
        if (ex.getCurator() != null && ex.getCurator().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Этот пользователь уже куратор выставки");
        }
        if (curatorIsBusy(userId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "У куратора уже " + MAX_ACTIVE_FOR_CURATOR + " активные выставки");
        }

        ex.setCurator(user);
        return mapper.toDto(ex);
    }

    private Exhibition requireExhibition(Long id) {
        return exhibitionRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Выставка не найдена"));
    }

    // считаю сколько у пользователя запланированных и открытых выставок
    private boolean curatorIsBusy(Long userId) {
        int count = 0;
        List<Exhibition> list = exhibitionRepository.findByCuratorId(userId);
        for (Exhibition e : list) {
            if (e.getStatus().equals("PLANNED") || e.getStatus().equals("OPEN")) {
                count++;
            }
        }
        return count >= MAX_ACTIVE_FOR_CURATOR;
    }
}
