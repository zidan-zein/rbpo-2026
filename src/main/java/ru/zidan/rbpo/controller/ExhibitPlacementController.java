package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.dto.ExhibitPlacementDto;
import ru.zidan.rbpo.service.ExhibitPlacementService;

import java.util.List;

@RestController
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
@RequestMapping("/api/exhibit-placements")
@RequiredArgsConstructor
public class ExhibitPlacementController {

    private final ExhibitPlacementService placementService;

    @GetMapping
    public ResponseEntity<List<ExhibitPlacementDto>> getAll() {
        return ResponseEntity.ok().body(placementService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitPlacementDto> getById(@PathVariable Long id) {
        var placement = placementService.getById(id);
        if (placement == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(placement);
    }

    // тут сервис может вернуть 400 (нет такого экспоната/выставки/зала)
    // или 409 (выставка уже не планируется, экспонат занят, зал закрыт или не хватает места)
    @PostMapping
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitPlacementDto> create(@RequestBody ExhibitPlacementDto placement) {
        var error = placementService.checkCreate(placement);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        var created = placementService.create(placement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<ExhibitPlacementDto> update(@PathVariable Long id, @RequestBody ExhibitPlacementDto placement) {
        if (placementService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = placementService.checkUpdate(id, placement);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.ok().body(placementService.update(id, placement));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole(\'ADMIN\')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (placementService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = placementService.checkDelete(id);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        placementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
