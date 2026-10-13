package com.vanshika.pdf_summarizer.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "uploads")
public class Upload {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "pdf_key", nullable = false)
    private String pdfKey;

    @Column(name = "summary_key", nullable = false)
    private String summaryKey;

    @Column(name = "status")
    private String status = "PROCESSING";

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "archived_at")
    private LocalDateTime archivedAt;
    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }

    public String getPdfKey() { return pdfKey; }
    public void setPdfKey(String pdfKey) { this.pdfKey = pdfKey; }

    public String getSummaryKey() { return summaryKey; }
    public void setSummaryKey(String summaryKey) { this.summaryKey = summaryKey; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getArchivedAt() {
        return archivedAt;
    }

    public void setArchivedAt(LocalDateTime archivedAt) {
        this.archivedAt = archivedAt;
    }
}

