package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.entity.ExhibitionHall;
import ru.zidan.rbpo.service.ExhibitionHallService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/exhibition-halls")
public class ExhibitionHallController {

    private final ExhibitionHallService hallService;

    @GetMapping
    public ResponseEntity<List<ExhibitionHall>> getAll() {
        return ResponseEntity.ok().body(hallService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExhibitionHall> getById(@PathVariable Long id) {
        var hall = hallService.getById(id);
        if (hall == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(hall);
    }

    @PostMapping
    public ResponseEntity<ExhibitionHall> create(@RequestBody ExhibitionHall hall) {
        var error = hallService.checkCreate(hall);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(hallService.create(hall));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ExhibitionHall> update(@PathVariable Long id, @RequestBody ExhibitionHall hall) {
        if (hallService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = hallService.checkUpdate(hall);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.ok().body(hallService.update(id, hall));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (hallService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        hallService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
