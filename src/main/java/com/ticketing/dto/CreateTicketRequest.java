package com.ticketing.dto;

import com.ticketing.model.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class CreateTicketRequest {

    @NotBlank(message = "Project code is required")
    private String projectCode;

    @NotBlank(message = "Title is required")
    private String title;

    private String description;

    private TicketPriority priority = TicketPriority.MEDIUM;

    private List<String> tags;

    private List<String> ideas; // Initial ideas for Plan scope

    public String getProjectCode() { return projectCode; }
    public void setProjectCode(String projectCode) { this.projectCode = projectCode; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TicketPriority getPriority() { return priority; }
    public void setPriority(TicketPriority priority) { this.priority = priority; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    public List<String> getIdeas() { return ideas; }
    public void setIdeas(List<String> ideas) { this.ideas = ideas; }
}
