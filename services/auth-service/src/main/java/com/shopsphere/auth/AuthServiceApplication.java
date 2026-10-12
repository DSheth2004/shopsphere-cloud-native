package com.shopsphere.auth;

import com.shopsphere.common.security.JwtTokens;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class AuthServiceApplication {
    public static void main(String[] args) { SpringApplication.run(AuthServiceApplication.class, args); }

    @Bean PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }

    @Bean JwtTokens jwtTokens(@Value("${shopsphere.jwt.secret}") String secret,
                              @Value("${shopsphere.jwt.ttl:PT8H}") Duration ttl) {
        return new JwtTokens(secret, ttl);
    }
}
