package com.vanshika.pdf_summarizer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "api_tokens")
public class ApiToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Which student owns this token
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // We will store a protected version of the token here
    @Column(name = "token_hash", nullable = false, unique = true)
    private String tokenHash;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public void setTokenHash(String tokenHash) {
        this.tokenHash = tokenHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}