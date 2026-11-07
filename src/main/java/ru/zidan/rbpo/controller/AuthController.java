package ru.zidan.rbpo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;
import ru.zidan.rbpo.entity.User;
import ru.zidan.rbpo.service.UserService;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final UserService userService;

    @GetMapping("/csrf")
    public CsrfToken csrf(CsrfToken token) {
        return token;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        var error = userService.checkCreate(user);
        if (error != null) {
            return new ResponseEntity<>(error);
        }
        var created = userService.create(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/me")
    public Map<String, Object> me(Authentication authentication) {
        Map<String, Object> result = new HashMap<>();
        result.put("username", authentication.getName());
        result.put("authorities", authentication.getAuthorities());
        return result;
    }
}
