package com.ticketing.dto;

import com.ticketing.model.TicketPriority;
import java.util.List;

public class UpdateTicketRequest {

    private String title;
    private String description;
    private TicketPriority priority;
    private List<String> tags;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
