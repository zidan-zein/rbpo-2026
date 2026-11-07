package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.dto.ExhibitionDto;
import ru.zidan.rbpo.service.ExhibitionService;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ResponseEntity<List<ExhibitionDto>> getAll() {
        return ResponseEntity.ok().body(exhibitionService.getAll());
    }

    // /api/exhibitions/1
    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionDto> getById(@PathVariable Long id) {
        var exhibition = exhibitionService.getById(id);
        if (exhibition == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(exhibition);
    }

    @PostMapping
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> create(@RequestBody ExhibitionDto exhibition) {
        var error = exhibitionService.checkCreate(exhibition);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(exhibitionService.create(exhibition));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> update(@PathVariable Long id, @RequestBody ExhibitionDto exhibition) {
        if (exhibitionService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = exhibitionService.checkUpdate(id, exhibition);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.ok().body(exhibitionService.update(id, exhibition));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (exhibitionService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = exhibitionService.checkDelete(id);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        exhibitionService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // дальше бизнес-операции, если их нельзя выполнить, сервис вернет 409 с причиной

    // /api/exhibitions/1/open
    @PostMapping("/{id}/open")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> open(@PathVariable Long id) {
        return ResponseEntity.ok().body(exhibitionService.open(id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> close(@PathVariable Long id) {
        return ResponseEntity.ok().body(exhibitionService.close(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> cancel(@PathVariable Long id) {
        return ResponseEntity.ok().body(exhibitionService.cancel(id));
    }

    // /api/exhibitions/3/curator/2
    @PutMapping("/{id}/curator/{userId}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitionDto> changeCurator(@PathVariable Long id, @PathVariable Long userId) {
        return ResponseEntity.ok().body(exhibitionService.changeCurator(id, userId));
    }
}
