package com.ticketing.dto;

import com.ticketing.model.TicketPhase;
import com.ticketing.model.TicketPriority;
import com.ticketing.model.TicketScope;
import java.time.Instant;
import java.util.List;

public class TicketSummaryResponse {

    private String id;
    private String projectCode;
    private String title;
    private String description;
    private TicketScope scope;
    private TicketPhase phase;
    private boolean completed;
    private String assignee;
    private String assigneeName;
    private String assigneeAvatarUrl;
    private String reporter;
    private String reporterName;
    private TicketPriority priority;
    private List<String> tags;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Instant promotedAt;
    private int ideaCount;
    private int activeIdeaCount;
    private int totalCheckpoints;
    private int completedCheckpoints;
    private int commentCount;
    private int attachmentCount;

    public TicketSummaryResponse() {}

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

    public String getAssigneeName() { return assigneeName; }
    public void setAssigneeName(String assigneeName) { this.assigneeName = assigneeName; }

    public String getAssigneeAvatarUrl() { return assigneeAvatarUrl; }
    public void setAssigneeAvatarUrl(String assigneeAvatarUrl) { this.assigneeAvatarUrl = assigneeAvatarUrl; }

    public String getReporter() { return reporter; }
    public void setReporter(String reporter) { this.reporter = reporter; }

    public String getReporterName() { return reporterName; }
    public void setReporterName(String reporterName) { this.reporterName = reporterName; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Instant getPromotedAt() { return promotedAt; }
    public void setPromotedAt(Instant promotedAt) { this.promotedAt = promotedAt; }

    public int getIdeaCount() { return ideaCount; }
    public void setIdeaCount(int ideaCount) { this.ideaCount = ideaCount; }

    public int getActiveIdeaCount() { return activeIdeaCount; }
    public void setActiveIdeaCount(int activeIdeaCount) { this.activeIdeaCount = activeIdeaCount; }

    public int getTotalCheckpoints() { return totalCheckpoints; }
    public void setTotalCheckpoints(int totalCheckpoints) { this.totalCheckpoints = totalCheckpoints; }

    public int getCompletedCheckpoints() { return completedCheckpoints; }
    public void setCompletedCheckpoints(int completedCheckpoints) { this.completedCheckpoints = completedCheckpoints; }

    public int getCommentCount() { return commentCount; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }

    public int getAttachmentCount() { return attachmentCount; }
    public void setAttachmentCount(int attachmentCount) { this.attachmentCount = attachmentCount; }
}
