package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class RelateTicketRequest {

    @NotBlank(message = "Target ticket ID is required")
    private String relatedTicketId;

    public String getRelatedTicketId() { return relatedTicketId; }
    public void setRelatedTicketId(String relatedTicketId) { this.relatedTicketId = relatedTicketId; }
}
