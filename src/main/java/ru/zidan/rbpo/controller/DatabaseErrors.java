package ru.zidan.rbpo.controller;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import java.util.Map;

@RestControllerAdvice
public class DatabaseErrors {
    // Вместо SQL и значений из запроса возвращаю короткий ответ.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflict(DataIntegrityViolationException error) {
        return ResponseEntity.status(409).body(Map.of("error", "Data conflict"));
    }
}
