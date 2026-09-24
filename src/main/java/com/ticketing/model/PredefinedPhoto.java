package com.ticketing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "photos")
public class PredefinedPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, length = 1000)
    private String url;

    // "USER", "PROJECT", "BOTH"
    @Column(nullable = false)
    private String category = "BOTH";

    @Column(name = "is_custom", nullable = false)
    private boolean isCustom = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public PredefinedPhoto() {}

    public PredefinedPhoto(String name, String url, String category, boolean isCustom) {
        this.name = name;
        this.url = url;
        this.category = category;
        this.isCustom = isCustom;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public boolean isCustom() { return isCustom; }
    public void setCustom(boolean custom) { isCustom = custom; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
