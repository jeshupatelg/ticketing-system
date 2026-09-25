package com.ticketing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_activities", indexes = {
    @Index(name = "idx_activity_ticket_id", columnList = "ticket_id"),
    @Index(name = "idx_activity_timestamp", columnList = "timestamp")
})
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", length = 32, nullable = false)
    private String ticketId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", length = 40, nullable = false)
    private TicketActivityType activityType;

    @Column(name = "username", length = 64)
    private String username;

    @Column(name = "user_display_name", length = 128)
    private String userDisplayName;

    @Column(name = "description", length = 512, nullable = false)
    private String description;

    @Column(name = "details", columnDefinition = "TEXT")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();

    public TicketActivity() {}

    public TicketActivity(String ticketId, TicketActivityType activityType, String username, String userDisplayName, String description, String details) {
        this.ticketId = ticketId;
        this.activityType = activityType;
        this.username = username;
        this.userDisplayName = userDisplayName;
        this.description = description;
        this.details = details;
        this.timestamp = Instant.now();
    }

    public TicketActivity(String ticketId, TicketActivityType activityType, String username, String userDisplayName, String description, String details, Instant timestamp) {
        this.ticketId = ticketId;
        this.activityType = activityType;
        this.username = username;
        this.userDisplayName = userDisplayName;
        this.description = description;
        this.details = details;
        this.timestamp = timestamp != null ? timestamp : Instant.now();
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

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }

    public Instant getTimestamp() { return timestamp; }
    public void setTimestamp(Instant timestamp) { this.timestamp = timestamp; }
}
