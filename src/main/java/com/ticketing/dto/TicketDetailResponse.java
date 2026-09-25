package com.ticketing.dto;

import com.ticketing.model.TicketAttachment;
import com.ticketing.model.TicketCheckpoint;
import com.ticketing.model.TicketComment;
import com.ticketing.model.TicketIdea;
import java.util.ArrayList;
import java.util.List;

public class TicketDetailResponse extends TicketSummaryResponse {

    private List<TicketIdea> ideas = new ArrayList<>();
    private List<TicketCheckpoint> checkpoints = new ArrayList<>();
    private List<TicketComment> comments = new ArrayList<>();
    private List<String> relatedTickets = new ArrayList<>();
    private List<TicketAttachment> attachments = new ArrayList<>();
    private List<TicketActivityResponse> activities = new ArrayList<>();

    public TicketDetailResponse() {}

    public List<TicketIdea> getIdeas() { return ideas; }
    public void setIdeas(List<TicketIdea> ideas) { this.ideas = ideas; }

    public List<TicketCheckpoint> getCheckpoints() { return checkpoints; }
    public void setCheckpoints(List<TicketCheckpoint> checkpoints) { this.checkpoints = checkpoints; }

    public List<TicketComment> getComments() { return comments; }
    public void setComments(List<TicketComment> comments) { this.comments = comments; }

    public List<String> getRelatedTickets() { return relatedTickets; }
    public void setRelatedTickets(List<String> relatedTickets) { this.relatedTickets = relatedTickets; }

    public List<TicketAttachment> getAttachments() { return attachments; }
    public void setAttachments(List<TicketAttachment> attachments) { this.attachments = attachments; }

    public List<TicketActivityResponse> getActivities() { return activities; }
    public void setActivities(List<TicketActivityResponse> activities) { this.activities = activities; }
}
