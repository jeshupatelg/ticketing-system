package com.ticketing.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;

@Entity
@Table(name = "projects")
public class Project {

    @Id
    @Column(name = "code", length = 3, nullable = false, unique = true)
    @Size(min = 3, max = 3, message = "Project code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Za-z0-9]{3}$", message = "Project code must contain only alphanumeric characters")
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "photo_url", length = 1000)
    private String photoUrl;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Project() {}

    public Project(String code, String name, String description, String photoUrl) {
        this.code = code != null ? code.toUpperCase() : null;
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase() : null; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
