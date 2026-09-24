package com.ticketing.controller;

import com.ticketing.config.UserContextHolder;
import com.ticketing.dto.UpdateAvatarRequest;
import com.ticketing.dto.UpdateThemeRequest;
import com.ticketing.dto.UserProfileResponse;
import com.ticketing.model.User;
import com.ticketing.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileResponse> getCurrentProfile() {
        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            // In server deployment, unauthenticated profile access is prohibited
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(userService.toProfileResponse(currentUser));
    }

    @PutMapping("/profile/theme")
    public ResponseEntity<UserProfileResponse> updateTheme(@Valid @RequestBody UpdateThemeRequest request) {
        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String username = currentUser.getUsername();
        User updated = userService.updateTheme(username, request.getTheme());
        return ResponseEntity.ok(userService.toProfileResponse(updated));
    }

    @PutMapping("/profile/avatar")
    public ResponseEntity<UserProfileResponse> updateAvatar(@Valid @RequestBody UpdateAvatarRequest request) {
        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String username = currentUser.getUsername();
        User updated = userService.updateAvatar(username, request.getAvatarUrl());
        return ResponseEntity.ok(userService.toProfileResponse(updated));
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserProfileResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.findAllUsers().stream()
                .map(userService::toProfileResponse)
                .collect(Collectors.toList()));
    }
}
