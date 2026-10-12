package com.shopsphere.auth;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** Phase-friendly local auth facade; replace its in-memory store with PostgreSQL in production. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final Map<String, String> users = new ConcurrentHashMap<>();
    @PostMapping("/register") @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> register(@Valid @RequestBody Credentials input) {
        if (users.putIfAbsent(input.email(), input.password()) != null) throw new IllegalArgumentException("Email already registered");
        return Map.of("email", input.email(), "role", "USER");
    }
    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Credentials input) {
        if (!input.password().equals(users.get(input.email()))) throw new IllegalArgumentException("Invalid credentials");
        return Map.of("accessToken", "local-demo-token", "tokenType", "Bearer");
    }
    public record Credentials(@Email String email, @NotBlank String password) { }
}
