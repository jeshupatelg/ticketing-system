package com.ticketing.dto;

import com.ticketing.model.TicketActivityType;
import java.time.Instant;

public class TicketActivityResponse {
    private Long id;
    private String ticketId;
    private TicketActivityType activityType;
    private String username;
    private String userDisplayName;
    private String userAvatarUrl;
    private String description;
    private String details;
    private Instant timestamp;

    public TicketActivityResponse() {}

    public TicketActivityResponse(Long id, String ticketId, TicketActivityType activityType, String username, String userDisplayName, String userAvatarUrl, String description, String details, Instant timestamp) {
        this.id = id;
        this.ticketId = ticketId;
        this.activityType = activityType;
        this.username = username;
        this.userDisplayName = userDisplayName;
        this.userAvatarUrl = userAvatarUrl;
        this.description = description;
        this.details = details;
        this.timestamp = timestamp;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public TicketActivityType getActivityType() { return activityType; }
    public void setActivityType(TicketActivityType activityType) { this.activityType = activityType; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getUserDisplayName() { return userDisplayName; }
    public void setUserDisplayName(String userDisplayName) { this.userDisplayName = userDisplayName; }

    public String getUserAvatarUrl() { return userAvatarUrl; }
    public void setUserAvatarUrl(String userAvatarUrl) { this.userAvatarUrl = userAvatarUrl; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
