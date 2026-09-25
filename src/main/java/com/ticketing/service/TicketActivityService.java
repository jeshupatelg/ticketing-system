package com.ticketing.service;

import com.ticketing.config.UserContextHolder;
import com.ticketing.dto.TicketActivityResponse;
import com.ticketing.model.TicketActivity;
import com.ticketing.model.TicketActivityType;
import com.ticketing.model.User;
import com.ticketing.repository.TicketActivityRepository;
import com.ticketing.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketActivityService {

    private final TicketActivityRepository activityRepository;
    private final UserRepository userRepository;

    public TicketActivityService(TicketActivityRepository activityRepository, UserRepository userRepository) {
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TicketActivity recordActivity(String ticketId, TicketActivityType type, String username, String description, String details) {
        return recordActivity(ticketId, type, username, description, details, Instant.now());
    }

    @Transactional
    public TicketActivity recordActivity(String ticketId, TicketActivityType type, String username, String description, String details, Instant timestamp) {
        if (username == null || username.isBlank()) {
            User currentUser = UserContextHolder.get();
            if (currentUser != null) {
                username = currentUser.getUsername();
            } else {
                username = "system";
            }
        }
        final String effectiveUser = username.trim().toLowerCase();
        String displayName = userRepository.findByUsername(effectiveUser)
                .map(User::getName)
                .orElse(effectiveUser);

        TicketActivity activity = new TicketActivity(ticketId, type, effectiveUser, displayName, description, details, timestamp);
        return activityRepository.save(activity);
    }

    public List<TicketActivityResponse> getActivitiesForTicket(String ticketId) {
        List<TicketActivity> list = activityRepository.findByTicketIdOrderByTimestampDesc(ticketId);
        return list.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public TicketActivityResponse toResponse(TicketActivity a) {
        String avatarUrl = "/api/photos/default/avatar-1.svg";
        if (a.getUsername() != null && !a.getUsername().isBlank()) {
            avatarUrl = userRepository.findByUsername(a.getUsername().toLowerCase())
                    .map(User::getAvatarUrl)
                    .orElse("/api/photos/default/avatar-1.svg");
        }
        return new TicketActivityResponse(
                a.getId(),
                a.getTicketId(),
                a.getActivityType(),
                a.getUsername(),
                a.getUserDisplayName(),
                avatarUrl,
                a.getDescription(),
                a.getDetails(),
                a.getTimestamp()
        );
    }
}
