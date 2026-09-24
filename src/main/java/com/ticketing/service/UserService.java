package com.ticketing.service;

import com.ticketing.dto.UserProfileResponse;
import com.ticketing.model.User;
import com.ticketing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User getOrCreateUser(String username, String name, String email) {
        if (username == null || username.isBlank()) {
            username = "anonymous";
        }
        final String effectiveUsername = username.trim().toLowerCase();
        final String effectiveName = (name != null && !name.isBlank()) ? name.trim() : effectiveUsername;

        return userRepository.findByUsername(effectiveUsername)
                .map(existing -> {
                    boolean changed = false;
                    if (existing.getName() == null || !existing.getName().equals(effectiveName)) {
                        existing.setName(effectiveName);
                        changed = true;
                    }
                    if (email != null && !email.isBlank() && !email.equals(existing.getEmail())) {
                        existing.setEmail(email);
                        changed = true;
                    }
                    return changed ? userRepository.save(existing) : existing;
                })
                .orElseGet(() -> {
                    String defaultAvatar = "/static/avatars/avatar-1.svg";
                    User newUser = new User(effectiveUsername, effectiveUsername, effectiveName, email, defaultAvatar);
                    return userRepository.save(newUser);
                });
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) return Optional.empty();
        return userRepository.findByUsername(username.trim().toLowerCase());
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User updateTheme(String username, String theme) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setThemePreference(theme);
        return userRepository.save(user);
    }

    @Transactional
    public User updateAvatar(String username, String avatarUrl) {
        User user = userRepository.findByUsername(username.toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));
        user.setAvatarUrl(avatarUrl);
        return userRepository.save(user);
    }

    public UserProfileResponse toProfileResponse(User user) {
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getThemePreference()
        );
    }
}
