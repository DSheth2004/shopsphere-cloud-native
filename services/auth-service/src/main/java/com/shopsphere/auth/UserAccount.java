package com.shopsphere.auth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "users")
public class UserAccount {
    @Id private String email;
    @Column(nullable = false) private String displayName;
    @Column(nullable = false) private String passwordHash;
    @Column(nullable = false) private String role;
    @Column(nullable = false) private Instant createdAt;

    protected UserAccount() { }

    public UserAccount(String email, String displayName, String passwordHash, String role) {
        this.email = email;
        this.displayName = displayName;
        this.passwordHash = passwordHash;
        this.role = role;
        this.createdAt = Instant.now();
    }

    public String getEmail() { return email; }
    public String getDisplayName() { return displayName; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public Instant getCreatedAt() { return createdAt; }
}
