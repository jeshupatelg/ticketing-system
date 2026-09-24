package com.ticketing.dto;

import com.ticketing.model.TicketPhase;
import jakarta.validation.constraints.NotNull;

public class TransitionPhaseRequest {

    @NotNull(message = "Target phase is required")
    private TicketPhase phase;

    // Required if transitioning to EXECUTION when ticket currently has no assignee
    private String assignee;

    // For CLOSED phase: true = completed successfully, false = cancelled / incomplete
    private Boolean completed;

    public TicketPhase getPhase() { return phase; }
    public void setPhase(TicketPhase phase) { this.phase = phase; }

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }

    public Boolean getCompleted() { return completed; }
    public void setCompleted(Boolean completed) { this.completed = completed; }
}
