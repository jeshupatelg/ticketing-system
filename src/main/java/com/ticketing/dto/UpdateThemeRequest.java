package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;

public class UpdateThemeRequest {

    @NotBlank(message = "Theme cannot be blank")
    private String theme;

    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
}
