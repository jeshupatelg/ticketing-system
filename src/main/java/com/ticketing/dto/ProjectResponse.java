package com.ticketing.dto;

import java.time.Instant;

public class ProjectResponse {

    private String code;
    private String name;
    private String description;
    private String photoUrl;
    private long ticketCount;
    private Instant createdAt;

    public ProjectResponse() {}

    public ProjectResponse(String code, String name, String description, String photoUrl, long ticketCount, Instant createdAt) {
        this.code = code;
        this.name = name;
        this.description = description;
        this.photoUrl = photoUrl;
        this.ticketCount = ticketCount;
        this.createdAt = createdAt;
    }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public long getTicketCount() { return ticketCount; }
    public void setTicketCount(long ticketCount) { this.ticketCount = ticketCount; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
