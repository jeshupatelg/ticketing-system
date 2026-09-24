package com.ticketing.controller;

import com.ticketing.config.UserContextHolder;
import com.ticketing.dto.*;
import com.ticketing.model.*;
import com.ticketing.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/projects/{projectCode}/tickets")
    public ResponseEntity<List<TicketSummaryResponse>> getProjectTickets(
            @PathVariable String projectCode,
            @RequestParam(defaultValue = "LIVE") TicketScope scope,
            @RequestParam(defaultValue = "false") boolean includeAllCompleted) {
        return ResponseEntity.ok(ticketService.getTicketsByProjectAndScope(projectCode, scope, includeAllCompleted));
    }

    @GetMapping("/tickets/{id}")
    public ResponseEntity<TicketDetailResponse> getTicket(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.getTicketDetail(id));
    }

    @PostMapping("/tickets")
    public ResponseEntity<TicketDetailResponse> createTicket(@Valid @RequestBody CreateTicketRequest request) {
        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String reporter = currentUser.getUsername();
        return ResponseEntity.ok(ticketService.createTicket(request, reporter));
    }

    @PutMapping("/tickets/{id}")
    public ResponseEntity<TicketDetailResponse> updateTicket(
            @PathVariable String id,
            @RequestBody UpdateTicketRequest request) {
        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PostMapping("/tickets/{id}/promote")
    public ResponseEntity<TicketDetailResponse> promoteTicket(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.promoteTicket(id));
    }

    @PostMapping("/tickets/{id}/cancel-plan")
    public ResponseEntity<TicketDetailResponse> cancelPlanTicket(@PathVariable String id) {
        return ResponseEntity.ok(ticketService.cancelPlanTicket(id));
    }

    @PostMapping("/tickets/{id}/transition")
    public ResponseEntity<TicketDetailResponse> transitionPhase(
            @PathVariable String id,
            @Valid @RequestBody TransitionPhaseRequest request) {
        return ResponseEntity.ok(ticketService.transitionPhase(id, request));
    }

    @PostMapping("/tickets/{id}/reassign")
    public ResponseEntity<TicketDetailResponse> reassignTicket(
            @PathVariable String id,
            @Valid @RequestBody ReassignRequest request) {
        return ResponseEntity.ok(ticketService.reassignTicket(id, request.getAssignee()));
    }

    // Plan ideas
    @PostMapping("/tickets/{id}/ideas")
    public ResponseEntity<TicketIdea> addIdea(
            @PathVariable String id,
            @Valid @RequestBody IdeaRequest request) {
        return ResponseEntity.ok(ticketService.addIdea(id, request));
    }

    @PutMapping("/tickets/{id}/ideas/{ideaId}")
    public ResponseEntity<TicketIdea> updateIdea(
            @PathVariable String id,
            @PathVariable Long ideaId,
            @Valid @RequestBody IdeaRequest request) {
        return ResponseEntity.ok(ticketService.updateIdea(id, ideaId, request));
    }

    @DeleteMapping("/tickets/{id}/ideas/{ideaId}")
    public ResponseEntity<Void> deleteIdea(
            @PathVariable String id,
            @PathVariable Long ideaId) {
        ticketService.deleteIdea(id, ideaId);
        return ResponseEntity.noContent().build();
    }

    // Checkpoints
    @PutMapping("/tickets/{id}/checkpoints/{checkpointId}")
    public ResponseEntity<TicketCheckpoint> toggleCheckpoint(
            @PathVariable String id,
            @PathVariable Long checkpointId,
            @RequestBody CheckpointToggleRequest request) {
        return ResponseEntity.ok(ticketService.toggleCheckpoint(id, checkpointId, request.isCompleted()));
    }

    // Comments
    @PostMapping("/tickets/{id}/comments")
    public ResponseEntity<TicketComment> addComment(
            @PathVariable String id,
            @Valid @RequestBody CommentRequest request) {
        User currentUser = UserContextHolder.get();
        if (currentUser == null) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED).build();
        }
        String authorUsername = currentUser.getUsername();
        String author = currentUser.getName();
        return ResponseEntity.ok(ticketService.addComment(id, authorUsername, author, request.getContent()));
    }

    // Related tickets
    @PostMapping("/tickets/{id}/related")
    public ResponseEntity<Void> linkRelated(
            @PathVariable String id,
            @Valid @RequestBody RelateTicketRequest request) {
        ticketService.linkRelatedTicket(id, request.getRelatedTicketId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/tickets/{id}/related/{relatedId}")
    public ResponseEntity<Void> unlinkRelated(
            @PathVariable String id,
            @PathVariable String relatedId) {
        ticketService.unlinkRelatedTicket(id, relatedId);
        return ResponseEntity.noContent().build();
    }

    // Metrics dashboard
    @GetMapping("/metrics")
    public ResponseEntity<MetricsResponse> getMetrics() {
        return ResponseEntity.ok(ticketService.getMetrics());
    }

    // Tags
    @GetMapping("/tags")
    public ResponseEntity<List<String>> getAllTags() {
        return ResponseEntity.ok(ticketService.getAllTags());
    }
}
