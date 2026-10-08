package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.entity.Exhibition;
import ru.zidan.rbpo.service.ExhibitionService;

import java.util.List;

@RestController
@RequestMapping("/api/exhibitions")
@RequiredArgsConstructor
public class ExhibitionController {

    private final ExhibitionService exhibitionService;

    @GetMapping
    public ResponseEntity<List<Exhibition>> getAll() {
        return ResponseEntity.ok().body(exhibitionService.getAll());
    }

    // /api/exhibitions/1
    @GetMapping("/{id}")
    public ResponseEntity<Exhibition> getById(@PathVariable Long id) {
        var exhibition = exhibitionService.getById(id);
        if (exhibition == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return ResponseEntity.ok().body(exhibition);
    }

    @PostMapping
    public ResponseEntity<Exhibition> create(@RequestBody Exhibition exhibition) {
        var error = exhibitionService.checkCreate(exhibition);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(exhibitionService.create(exhibition));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Exhibition> update(@PathVariable Long id, @RequestBody Exhibition exhibition) {
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
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (exhibitionService.getById(id) == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        exhibitionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
