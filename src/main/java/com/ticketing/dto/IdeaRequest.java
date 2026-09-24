package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class IdeaRequest {

    @NotBlank(message = "Idea content cannot be blank")
    private String content;

    private boolean active = true;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
