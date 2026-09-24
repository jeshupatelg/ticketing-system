package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateAvatarRequest {

    @NotBlank(message = "Avatar URL cannot be blank")
    private String avatarUrl;

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
