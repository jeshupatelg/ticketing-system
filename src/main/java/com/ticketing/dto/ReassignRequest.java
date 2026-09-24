package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class ReassignRequest {

    @NotBlank(message = "Assignee is required and cannot be de-assigned")
    private String assignee;

    public String getAssignee() { return assignee; }
    public void setAssignee(String assignee) { this.assignee = assignee; }
}
