package com.vanshika.pdf_summarizer.model;

import jakarta.persistence.*;//this
import java.time.LocalDateTime;

@Entity//th
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)//this tells the database to automatically generate a unique value for this field when a new user is created. The IDENTITY strategy means that the database will handle the generation of the primary key, usually by using an auto-incrementing column.
    private Long id;

    @Column(name = "google_id", unique = true, nullable = false)
    private String googleId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist//this is a JPA annotation that marks a method to be executed before the entity is persisted (saved) to the database. In this case, it sets the createdAt field to the current date and time when a new user is created.
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getGoogleId() { return googleId; }
    public void setGoogleId(String googleId) { this.googleId = googleId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
