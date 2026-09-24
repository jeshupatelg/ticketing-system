package com.ticketing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CreateProjectRequest {

    @NotBlank(message = "Project code is required")
    @Size(min = 3, max = 3, message = "Project code must be exactly 3 characters")
    @Pattern(regexp = "^[A-Za-z0-9]{3}$", message = "Project code must be 3 alphanumeric characters")
    private String code;

    @NotBlank(message = "Project name is required")
    private String name;

    private String description;

    private String photoUrl;

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code != null ? code.toUpperCase() : null; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
}
