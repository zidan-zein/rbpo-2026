package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.entity.ExhibitPlacement;
import ru.zidan.rbpo.service.ExhibitPlacementService;

import java.util.List;

@RestController
@RequestMapping("/api/exhibit-placements")
@RequiredArgsConstructor
public class ExhibitPlacementController {

    private final ExhibitPlacementService placementService;

    @GetMapping
    public ResponseEntity<List<ExhibitPlacement>> getAll() {
        return ResponseEntity.ok().body(placementService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitPlacement> getById(@PathVariable Long id) {
        var placement = placementService.getById(id);
        if (placement == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(placement);
    }

    // тут сервис может вернуть 400 (нет такого экспоната/выставки/зала)
    // или 409 (зал закрыт или в нем не хватает места)
    @PostMapping
    public ResponseEntity<ExhibitPlacement> create(@RequestBody ExhibitPlacement placement) {
        var error = placementService.checkCreate(placement);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        var created = placementService.create(placement);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExhibitPlacement> update(@PathVariable Long id, @RequestBody ExhibitPlacement placement) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (placementService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        placementService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
