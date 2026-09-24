package com.ticketing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "tickets")
public class Ticket {

    @Id
    @Column(name = "id", length = 32, nullable = false, unique = true)
    private String id; // Pattern: <project-code>-<5-digit sequence>, e.g. ADH-00001

    @Column(name = "project_code", length = 3, nullable = false)
    private String projectCode;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketScope scope = TicketScope.PLAN;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPhase phase = TicketPhase.PLAN;

    /**
     * For closed/cancelled tickets:
     * true = completed successfully
     * false = cancelled / incomplete (triggers red color styling)
     */
    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "assignee")
    private String assignee; // username of assigned user (null by default in Planned)

    @Column(name = "reporter")
    private String reporter;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority = TicketPriority.MEDIUM;

    @Column(columnDefinition = "TEXT")
    private String tags = ""; // comma-separated or JSON list of tags

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    @Column(name = "promoted_at")
    private Instant promotedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Ticket() {}

    public Ticket(String id, String projectCode, String title, String description, String reporter) {
        this.id = id;
        this.projectCode = projectCode;
        this.title = title;
        this.description = description;
        this.reporter = reporter;
        this.scope = TicketScope.PLAN;
        this.phase = TicketPhase.PLAN;
        this.completed = false;
        this.assignee = null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketScope getScope() { return scope; }
    public void setScope(TicketScope scope) { this.scope = scope; }

    public TicketPhase getPhase() { return phase; }
    public void setPhase(TicketPhase phase) { this.phase = phase; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Instant promotedAt) { this.promotedAt = promotedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = Instant.now();
    }
}
