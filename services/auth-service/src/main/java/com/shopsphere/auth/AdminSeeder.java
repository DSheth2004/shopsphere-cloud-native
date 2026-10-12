package com.shopsphere.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the catalogue administrator on first start; the password comes from a secret, never from git. */
@Component
class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder passwords;
    private final String email;
    private final String password;

    AdminSeeder(UserRepository users, PasswordEncoder passwords,
                @Value("${shopsphere.admin.email:admin@shopsphere.local}") String email,
                @Value("${shopsphere.admin.password:}") String password) {
        this.users = users;
        this.passwords = passwords;
        this.email = email;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (password.isBlank()) {
            log.warn("ADMIN_PASSWORD not set; no administrator account was created");
        } else if (!users.existsById(email)) {
            users.save(new UserAccount(email, "Store Admin", passwords.encode(password), "ADMIN"));
            log.info("Created administrator account {}", email);
        }
    }
}
