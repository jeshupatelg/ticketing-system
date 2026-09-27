package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class CheckpointRequest {

    @NotBlank(message = "Checkpoint title cannot be blank")
    private String title;

    public CheckpointRequest() {}

    public CheckpointRequest(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
