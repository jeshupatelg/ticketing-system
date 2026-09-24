package com.ticketing.dto;

public class UserProfileResponse {

    private String id;
    private String username;
    private String name;
    private String email;
    private String avatarUrl;
    private String themePreference;

    public UserProfileResponse() {}

    public UserProfileResponse(String id, String username, String name, String email, String avatarUrl, String themePreference) {
        this.id = id;
        this.username = username;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
        this.themePreference = themePreference;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getThemePreference() { return themePreference; }
    public void setThemePreference(String themePreference) { this.themePreference = themePreference; }
}
