package com.ticketing.dto;

import java.util.Map;

public class MetricsResponse {

    private long totalTickets;
    private long planScopeTickets;
    private long liveScopeTickets;
    private long plannedPhaseTickets;
    private long executionPhaseTickets;
    private long closedPhaseTickets;
    private long completedTickets;
    private long cancelledTickets;
    private long totalProjects;
    private Map<String, Long> projectTicketCounts;

    public MetricsResponse() {}

    public long getTotalTickets() { return totalTickets; }
    public void setTotalTickets(long totalTickets) { this.totalTickets = totalTickets; }

    public long getPlanScopeTickets() { return planScopeTickets; }
    public void setPlanScopeTickets(long planScopeTickets) { this.planScopeTickets = planScopeTickets; }

    public long getLiveScopeTickets() { return liveScopeTickets; }
    public void setLiveScopeTickets(long liveScopeTickets) { this.liveScopeTickets = liveScopeTickets; }

    public long getPlannedPhaseTickets() { return plannedPhaseTickets; }
    public void setPlannedPhaseTickets(long plannedPhaseTickets) { this.plannedPhaseTickets = plannedPhaseTickets; }

    public long getExecutionPhaseTickets() { return executionPhaseTickets; }
    public void setExecutionPhaseTickets(long executionPhaseTickets) { this.executionPhaseTickets = executionPhaseTickets; }

    public long getClosedPhaseTickets() { return closedPhaseTickets; }
    public void setClosedPhaseTickets(long closedPhaseTickets) { this.closedPhaseTickets = closedPhaseTickets; }

    public long getCompletedTickets() { return completedTickets; }
    public void setCompletedTickets(long completedTickets) { this.completedTickets = completedTickets; }

    public long getCancelledTickets() { return cancelledTickets; }
    public void setCancelledTickets(long cancelledTickets) { this.cancelledTickets = cancelledTickets; }

    public long getTotalProjects() { return totalProjects; }
    public void setTotalProjects(long totalProjects) { this.totalProjects = totalProjects; }

    public Map<String, Long> getProjectTicketCounts() { return projectTicketCounts; }
    public void setProjectTicketCounts(Map<String, Long> projectTicketCounts) { this.projectTicketCounts = projectTicketCounts; }
}
