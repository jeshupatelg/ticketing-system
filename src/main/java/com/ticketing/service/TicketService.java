package com.ticketing.service;

import com.ticketing.dto.*;
import com.ticketing.model.*;
import com.ticketing.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final ProjectRepository projectRepository;
    private final ProjectService projectService;
    private final TicketIdeaRepository ideaRepository;
    private final TicketCheckpointRepository checkpointRepository;
    private final TicketCommentRepository commentRepository;
    private final RelatedTicketRepository relatedTicketRepository;
    private final TicketAttachmentRepository attachmentRepository;
    private final UserRepository userRepository;

    public TicketService(TicketRepository ticketRepository,
                         ProjectRepository projectRepository,
                         ProjectService projectService,
                         TicketIdeaRepository ideaRepository,
                         TicketCheckpointRepository checkpointRepository,
                         TicketCommentRepository commentRepository,
                         RelatedTicketRepository relatedTicketRepository,
                         TicketAttachmentRepository attachmentRepository,
                         UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.projectRepository = projectRepository;
        this.projectService = projectService;
        this.ideaRepository = ideaRepository;
        this.checkpointRepository = checkpointRepository;
        this.commentRepository = commentRepository;
        this.relatedTicketRepository = relatedTicketRepository;
        this.attachmentRepository = attachmentRepository;
        this.userRepository = userRepository;
    }

    public List<TicketSummaryResponse> getTicketsByProjectAndScope(String projectCode, TicketScope scope, boolean includeAllCompleted) {
        String pCode = projectCode.toUpperCase();
        List<Ticket> tickets;

        if (scope == TicketScope.PLAN) {
            tickets = ticketRepository.findByProjectCodeAndScope(pCode, TicketScope.PLAN);
        } else {
            if (includeAllCompleted) {
                tickets = ticketRepository.findByProjectCodeAndScope(pCode, TicketScope.LIVE);
            } else {
                Instant oneWeekAgo = Instant.now().minus(7, ChronoUnit.DAYS);
                tickets = ticketRepository.findLiveTicketsDefault(pCode, oneWeekAgo);
            }
        }

        return tickets.stream().map(this::toSummaryResponse).collect(Collectors.toList());
    }

    public TicketDetailResponse getTicketDetail(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        TicketDetailResponse response = toDetailResponse(ticket);
        return response;
    }

    @Transactional
    public TicketDetailResponse createTicket(CreateTicketRequest request, String reporter) {
        String projectCode = request.getProjectCode().trim().toUpperCase();
        if (!projectRepository.existsByCodeIgnoreCase(projectCode)) {
            throw new IllegalArgumentException("Project does not exist: " + projectCode);
        }

        String ticketId = projectService.getNextTicketId(projectCode);

        Ticket ticket = new Ticket(
                ticketId,
                projectCode,
                request.getTitle().trim(),
                request.getDescription(),
                reporter
        );

        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }

        if (request.getTags() != null && !request.getTags().isEmpty()) {
            ticket.setTags(String.join(",", request.getTags()));
        }

        // Starts in Plan scope and Plan phase
        ticket.setScope(TicketScope.PLAN);
        ticket.setPhase(TicketPhase.PLAN);
        ticket.setCompleted(false);

        Ticket savedTicket = ticketRepository.save(ticket);

        // Add initial ideas if provided
        if (request.getIdeas() != null) {
            int order = 0;
            for (String ideaText : request.getIdeas()) {
                if (ideaText != null && !ideaText.isBlank()) {
                    TicketIdea idea = new TicketIdea(ticketId, ideaText.trim(), true, order++);
                    ideaRepository.save(idea);
                }
            }
        }

        return toDetailResponse(savedTicket);
    }

    @Transactional
    public TicketDetailResponse updateTicket(String ticketId, UpdateTicketRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Closed tickets are immutable and cannot be edited.");
        }

        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            ticket.setTitle(request.getTitle().trim());
        }
        if (request.getDescription() != null) {
            ticket.setDescription(request.getDescription());
        }
        if (request.getPriority() != null) {
            ticket.setPriority(request.getPriority());
        }
        if (request.getTags() != null) {
            ticket.setTags(String.join(",", request.getTags()));
        }

        return toDetailResponse(ticketRepository.save(ticket));
    }

    /**
     * Promote ticket from Plan scope to Live scope.
     * Ideas are sealed for modification. Active ideas become boolean Checkpoints.
     */
    @Transactional
    public TicketDetailResponse promoteTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.PLAN) {
            throw new IllegalStateException("Only tickets in Plan scope can be promoted.");
        }

        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Cancelled Plan tickets cannot be promoted.");
        }

        ticket.setScope(TicketScope.LIVE);
        ticket.setPhase(TicketPhase.PLANNED);
        ticket.setPromotedAt(Instant.now());

        // Copy active ideas to checkpoints
        List<TicketIdea> ideas = ideaRepository.findByTicketIdOrderByOrderIndexAsc(ticketId);
        int checkpointOrder = 0;
        for (TicketIdea idea : ideas) {
            if (idea.isActive()) {
                TicketCheckpoint cp = new TicketCheckpoint(ticketId, idea.getContent(), false, checkpointOrder++);
                checkpointRepository.save(cp);
            }
        }

        return toDetailResponse(ticketRepository.save(ticket));
    }

    /**
     * Cancel a Plan ticket.
     */
    @Transactional
    public TicketDetailResponse cancelPlanTicket(String ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.PLAN) {
            throw new IllegalStateException("Ticket is not in Plan scope.");
        }

        ticket.setPhase(TicketPhase.CLOSED);
        ticket.setCompleted(false);
        ticket.setCompletedAt(Instant.now());

        return toDetailResponse(ticketRepository.save(ticket));
    }

    /**
     * Transition Live ticket phase:
     * - Interchangeable between PLANNED and EXECUTION.
     * - Moving PLANNED -> EXECUTION when none assigned requires assignee.
     * - CLOSED tickets are immutable.
     * - Live tickets can be cancelled (completed=false) or completed (completed=true).
     */
    @Transactional
    public TicketDetailResponse transitionPhase(String ticketId, TransitionPhaseRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.LIVE) {
            throw new IllegalStateException("Ticket is not in Live scope. Promote or cancel in Plan scope first.");
        }

        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Closed tickets are immutable. Phase cannot be changed.");
        }

        TicketPhase targetPhase = request.getPhase();

        if (targetPhase == TicketPhase.PLAN) {
            throw new IllegalArgumentException("Cannot transition a Live ticket back to Plan phase.");
        }

        if (targetPhase == TicketPhase.EXECUTION) {
            // First move from Planned to Execution when none assigned, prompts assignment necessarily
            String assignee = request.getAssignee();
            if (assignee != null && !assignee.isBlank()) {
                ticket.setAssignee(assignee.trim().toLowerCase());
            }

            if (ticket.getAssignee() == null || ticket.getAssignee().isBlank()) {
                throw new IllegalArgumentException("An assignee must be specified when transitioning to Execution.");
            }
            ticket.setPhase(TicketPhase.EXECUTION);
        } else if (targetPhase == TicketPhase.PLANNED) {
            ticket.setPhase(TicketPhase.PLANNED);
        } else if (targetPhase == TicketPhase.CLOSED) {
            ticket.setPhase(TicketPhase.CLOSED);
            ticket.setCompleted(request.getCompleted() != null ? request.getCompleted() : true);
            ticket.setCompletedAt(Instant.now());
        }

        return toDetailResponse(ticketRepository.save(ticket));
    }

    /**
     * Reassign ticket in Execution phase:
     * "In execution, can be reassigned to other but not de-assigned."
     * "For Planned phase, Checkpoints and Assignee are immutable."
     */
    @Transactional
    public TicketDetailResponse reassignTicket(String ticketId, String newAssignee) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getPhase() == TicketPhase.PLANNED) {
            throw new IllegalStateException("Assignee is immutable during Planned phase.");
        }

        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Closed tickets are immutable.");
        }

        if (ticket.getPhase() != TicketPhase.EXECUTION) {
            throw new IllegalStateException("Assignee can only be changed during Execution phase.");
        }

        if (newAssignee == null || newAssignee.isBlank()) {
            throw new IllegalArgumentException("Ticket cannot be de-assigned in Execution phase.");
        }

        ticket.setAssignee(newAssignee.trim().toLowerCase());
        return toDetailResponse(ticketRepository.save(ticket));
    }

    /**
     * Plan Ideas Management:
     * Valid only when in Plan scope and not closed.
     */
    @Transactional
    public TicketIdea addIdea(String ticketId, IdeaRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.PLAN) {
            throw new IllegalStateException("Ideas can only be added to tickets in Plan scope.");
        }
        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Cannot add ideas to a cancelled Plan ticket.");
        }

        List<TicketIdea> existing = ideaRepository.findByTicketIdOrderByOrderIndexAsc(ticketId);
        TicketIdea idea = new TicketIdea(ticketId, request.getContent().trim(), request.isActive(), existing.size());
        return ideaRepository.save(idea);
    }

    @Transactional
    public TicketIdea updateIdea(String ticketId, Long ideaId, IdeaRequest request) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.PLAN) {
            throw new IllegalStateException("Ideas are sealed and cannot be modified once promoted to Live.");
        }
        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Cannot edit ideas in a closed ticket.");
        }

        TicketIdea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new IllegalArgumentException("Idea not found: " + ideaId));

        if (!idea.getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Idea does not belong to ticket: " + ticketId);
        }

        if (request.getContent() != null && !request.getContent().isBlank()) {
            idea.setContent(request.getContent().trim());
        }
        idea.setActive(request.isActive());
        return ideaRepository.save(idea);
    }

    @Transactional
    public void deleteIdea(String ticketId, Long ideaId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getScope() != TicketScope.PLAN) {
            throw new IllegalStateException("Ideas are sealed and cannot be removed once promoted to Live.");
        }

        TicketIdea idea = ideaRepository.findById(ideaId)
                .orElseThrow(() -> new IllegalArgumentException("Idea not found: " + ideaId));

        if (!idea.getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Idea does not belong to ticket: " + ticketId);
        }

        ideaRepository.delete(idea);
    }

    /**
     * Checkpoint Management:
     * For Planned phase: Checkpoints are IMMUTABLE.
     * In Execution: can toggle completed.
     * In Closed: IMMUTABLE.
     */
    @Transactional
    public TicketCheckpoint toggleCheckpoint(String ticketId, Long checkpointId, boolean completed) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        if (ticket.getPhase() == TicketPhase.PLANNED) {
            throw new IllegalStateException("Checkpoints are immutable during Planned phase.");
        }
        if (ticket.getPhase() == TicketPhase.CLOSED) {
            throw new IllegalStateException("Checkpoints cannot be modified on Closed tickets.");
        }
        if (ticket.getPhase() != TicketPhase.EXECUTION) {
            throw new IllegalStateException("Checkpoints can only be toggled in Execution phase.");
        }

        TicketCheckpoint checkpoint = checkpointRepository.findById(checkpointId)
                .orElseThrow(() -> new IllegalArgumentException("Checkpoint not found: " + checkpointId));

        if (!checkpoint.getTicketId().equals(ticketId)) {
            throw new IllegalArgumentException("Checkpoint does not belong to ticket: " + ticketId);
        }

        checkpoint.setCompleted(completed);
        return checkpointRepository.save(checkpoint);
    }

    /**
     * Comments:
     * Mutable for all phases!
     */
    @Transactional
    public TicketComment addComment(String ticketId, String author, String authorAvatarUrl, String content) {
        ticketRepository.findById(ticketId)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + ticketId));

        TicketComment comment = new TicketComment(ticketId, author, authorAvatarUrl, content.trim());
        return commentRepository.save(comment);
    }

    /**
     * Related Tickets:
     * Link to n other tickets. Reference only.
     */
    @Transactional
    public void linkRelatedTicket(String ticketId, String relatedTicketId) {
        if (ticketId.equalsIgnoreCase(relatedTicketId)) {
            throw new IllegalArgumentException("A ticket cannot be related to itself.");
        }

        if (!ticketRepository.existsById(ticketId)) {
            throw new IllegalArgumentException("Source ticket not found: " + ticketId);
        }
        if (!ticketRepository.existsById(relatedTicketId)) {
            throw new IllegalArgumentException("Related ticket not found: " + relatedTicketId);
        }

        if (relatedTicketRepository.findByTicketIdAndRelatedTicketId(ticketId, relatedTicketId).isEmpty()) {
            relatedTicketRepository.save(new RelatedTicket(ticketId, relatedTicketId));
        }
        // Bi-directional reference for convenience
        if (relatedTicketRepository.findByTicketIdAndRelatedTicketId(relatedTicketId, ticketId).isEmpty()) {
            relatedTicketRepository.save(new RelatedTicket(relatedTicketId, ticketId));
        }
    }

    @Transactional
    public void unlinkRelatedTicket(String ticketId, String relatedTicketId) {
        relatedTicketRepository.deleteByTicketIdAndRelatedTicketId(ticketId, relatedTicketId);
        relatedTicketRepository.deleteByTicketIdAndRelatedTicketId(relatedTicketId, ticketId);
    }

    /**
     * Metrics Dashboard calculation
     */
    public MetricsResponse getMetrics() {
        MetricsResponse metrics = new MetricsResponse();
        metrics.setTotalTickets(ticketRepository.count());
        metrics.setPlanScopeTickets(ticketRepository.countByScope(TicketScope.PLAN));
        metrics.setLiveScopeTickets(ticketRepository.countByScope(TicketScope.LIVE));
        metrics.setPlannedPhaseTickets(ticketRepository.countByPhase(TicketPhase.PLANNED));
        metrics.setExecutionPhaseTickets(ticketRepository.countByPhase(TicketPhase.EXECUTION));
        metrics.setClosedPhaseTickets(ticketRepository.countByPhase(TicketPhase.CLOSED));
        metrics.setCompletedTickets(ticketRepository.countByCompleted(true));
        metrics.setCancelledTickets(ticketRepository.countByPhase(TicketPhase.CLOSED) - ticketRepository.countByCompleted(true));
        metrics.setTotalProjects(projectRepository.count());

        Map<String, Long> projectCounts = new HashMap<>();
        for (Project p : projectRepository.findAll()) {
            projectCounts.put(p.getCode(), ticketRepository.countByProjectCode(p.getCode()));
        }
        metrics.setProjectTicketCounts(projectCounts);

        return metrics;
    }

    public List<String> getAllTags() {
        Set<String> tagSet = new HashSet<>();
        for (String raw : ticketRepository.findAllTagsRaw()) {
            if (raw != null && !raw.isBlank()) {
                for (String t : raw.split(",")) {
                    if (!t.trim().isEmpty()) {
                        tagSet.add(t.trim());
                    }
                }
            }
        }
        List<String> list = new ArrayList<>(tagSet);
        Collections.sort(list);
        return list;
    }

    private TicketSummaryResponse toSummaryResponse(Ticket ticket) {
        TicketSummaryResponse dto = new TicketSummaryResponse();
        dto.setId(ticket.getId());
        dto.setProjectCode(ticket.getProjectCode());
        dto.setTitle(ticket.getTitle());
        dto.setDescription(ticket.getDescription());
        dto.setScope(ticket.getScope());
        dto.setPhase(ticket.getPhase());
        dto.setCompleted(ticket.isCompleted());
        dto.setAssignee(ticket.getAssignee());
        dto.setReporter(ticket.getReporter());
        dto.setPriority(ticket.getPriority());
        dto.setCreatedAt(ticket.getCreatedAt());
        dto.setUpdatedAt(ticket.getUpdatedAt());
        dto.setCompletedAt(ticket.getCompletedAt());
        dto.setPromotedAt(ticket.getPromotedAt());

        if (ticket.getTags() != null && !ticket.getTags().isBlank()) {
            dto.setTags(Arrays.asList(ticket.getTags().split(",")));
        } else {
            dto.setTags(Collections.emptyList());
        }

        // Enrich user details
        if (ticket.getAssignee() != null) {
            userRepository.findByUsername(ticket.getAssignee()).ifPresent(u -> {
                dto.setAssigneeName(u.getName());
                dto.setAssigneeAvatarUrl(u.getAvatarUrl());
            });
        }
        if (ticket.getReporter() != null) {
            userRepository.findByUsername(ticket.getReporter()).ifPresent(u -> {
                dto.setReporterName(u.getName());
            });
        }

        // Counts
        if (ticket.getScope() == TicketScope.PLAN) {
            List<TicketIdea> ideas = ideaRepository.findByTicketIdOrderByOrderIndexAsc(ticket.getId());
            dto.setIdeaCount(ideas.size());
            dto.setActiveIdeaCount((int) ideas.stream().filter(TicketIdea::isActive).count());
        } else {
            List<TicketCheckpoint> cps = checkpointRepository.findByTicketIdOrderByOrderIndexAsc(ticket.getId());
            dto.setTotalCheckpoints(cps.size());
            dto.setCompletedCheckpoints((int) cps.stream().filter(TicketCheckpoint::isCompleted).count());
        }

        dto.setAttachmentCount((int) attachmentRepository.countByTicketId(ticket.getId()));
        dto.setCommentCount(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()).size());

        return dto;
    }

    private TicketDetailResponse toDetailResponse(Ticket ticket) {
        TicketSummaryResponse summary = toSummaryResponse(ticket);
        TicketDetailResponse detail = new TicketDetailResponse();

        // Copy summary fields
        detail.setId(summary.getId());
        detail.setProjectCode(summary.getProjectCode());
        detail.setTitle(summary.getTitle());
        detail.setDescription(summary.getDescription());
        detail.setScope(summary.getScope());
        detail.setPhase(summary.getPhase());
        detail.setCompleted(summary.isCompleted());
        detail.setAssignee(summary.getAssignee());
        detail.setAssigneeName(summary.getAssigneeName());
        detail.setAssigneeAvatarUrl(summary.getAssigneeAvatarUrl());
        detail.setReporter(summary.getReporter());
        detail.setReporterName(summary.getReporterName());
        detail.setPriority(summary.getPriority());
        detail.setTags(summary.getTags());
        detail.setCreatedAt(summary.getCreatedAt());
        detail.setUpdatedAt(summary.getUpdatedAt());
        detail.setCompletedAt(summary.getCompletedAt());
        detail.setPromotedAt(summary.getPromotedAt());
        detail.setIdeaCount(summary.getIdeaCount());
        detail.setActiveIdeaCount(summary.getActiveIdeaCount());
        detail.setTotalCheckpoints(summary.getTotalCheckpoints());
        detail.setCompletedCheckpoints(summary.getCompletedCheckpoints());
        detail.setCommentCount(summary.getCommentCount());
        detail.setAttachmentCount(summary.getAttachmentCount());

        // Fill detail collections
        detail.setIdeas(ideaRepository.findByTicketIdOrderByOrderIndexAsc(ticket.getId()));
        detail.setCheckpoints(checkpointRepository.findByTicketIdOrderByOrderIndexAsc(ticket.getId()));
        detail.setComments(commentRepository.findByTicketIdOrderByCreatedAtAsc(ticket.getId()));

        List<String> related = relatedTicketRepository.findByTicketId(ticket.getId()).stream()
                .map(RelatedTicket::getRelatedTicketId)
                .collect(Collectors.toList());
        detail.setRelatedTickets(related);

        detail.setAttachments(attachmentRepository.findByTicketIdOrderByUploadedAtAsc(ticket.getId()));

        return detail;
    }
}
