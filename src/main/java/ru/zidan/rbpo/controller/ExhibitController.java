package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.entity.Exhibit;
import ru.zidan.rbpo.service.ExhibitService;

import java.util.List;

// контроллер только принимает запрос и отдает ответ,
// всю логику делает сервис
@RestController
@RequestMapping("/api/exhibits")
@RequiredArgsConstructor
public class ExhibitController {

    private final ExhibitService exhibitService;

    @GetMapping
    public ResponseEntity<List<Exhibit>> getAll() {
        return ResponseEntity.ok().body(exhibitService.getAll());
    }

    // /api/exhibits/1
    @GetMapping("/{id}")
    public ResponseEntity<Exhibit> getById(@PathVariable Long id) {
        var exhibit = exhibitService.getById(id);
        if (exhibit == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(exhibit);
    }

    // при создании нужно вернуть 201, а не 200
    @PostMapping
    public ResponseEntity<Exhibit> create(@RequestBody Exhibit exhibit) {
        var error = exhibitService.checkCreate(exhibit);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        var created = exhibitService.create(exhibit);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Exhibit> update(@PathVariable Long id, @RequestBody Exhibit exhibit) {
        if (exhibitService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        var error = exhibitService.checkUpdate(exhibit);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.ok().body(exhibitService.update(id, exhibit));
    }

    // 204 - удалили, тело ответа пустое
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (exhibitService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        exhibitService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
