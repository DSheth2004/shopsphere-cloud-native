package com.shopsphere.auth;

import com.shopsphere.common.security.JwtTokens;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/** Registration and login backed by PostgreSQL; passwords are stored as BCrypt hashes only. */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final JwtTokens tokens;

    AuthController(UserRepository users, PasswordEncoder passwords, JwtTokens tokens) {
        this.users = users;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Session register(@Valid @RequestBody Registration input) {
        String email = input.email().trim().toLowerCase(Locale.ROOT);
        if (users.existsById(email)) throw new IllegalStateException("Email already registered");
        UserAccount user = users.save(new UserAccount(email, input.name().trim(), passwords.encode(input.password()), "USER"));
        return session(user);
    }

    @PostMapping("/login")
    public Session login(@Valid @RequestBody Credentials input) {
        UserAccount user = users.findById(input.email().trim().toLowerCase(Locale.ROOT))
                .filter(found -> passwords.matches(input.password(), found.getPasswordHash()))
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
        return session(user);
    }

    @GetMapping("/me")
    public Profile me(@RequestHeader("X-User-Id") String userId) {
        UserAccount user = users.findById(userId).orElseThrow(() -> new NoSuchElementException("User not found"));
        return new Profile(user.getEmail(), user.getDisplayName(), user.getRole());
    }

    private Session session(UserAccount user) {
        return new Session(tokens.issue(user.getEmail(), user.getRole()), "Bearer", tokens.ttl().toSeconds(),
                new Profile(user.getEmail(), user.getDisplayName(), user.getRole()));
    }

    public record Registration(@NotBlank @Size(max = 60) String name, @NotBlank @Email String email,
                               @NotBlank @Size(min = 8, max = 72) String password) { }
    public record Credentials(@NotBlank @Email String email, @NotBlank String password) { }
    public record Profile(String email, String name, String role) { }
    public record Session(String accessToken, String tokenType, long expiresIn, Profile user) { }
}
