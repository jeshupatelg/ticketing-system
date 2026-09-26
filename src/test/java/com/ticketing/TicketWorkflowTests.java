package com.ticketing;

import com.ticketing.controller.PhotoController;
import com.ticketing.dto.*;
import com.ticketing.model.*;
import com.ticketing.service.PhotoService;
import com.ticketing.service.ProjectService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class TicketWorkflowTests {

    @Autowired
    private ProjectService projectService;

    @Autowired
    private TicketService ticketService;

    @Autowired
    private UserService userService;

    @Autowired
    private PhotoService photoService;

    @Autowired
    private PhotoController photoController;

    @BeforeEach
    void setUp() {
        // Ensure user exists
        userService.getOrCreateUser("admin", "Admin User", "admin@ticketing.local");
        userService.getOrCreateUser("developer1", "Dev One", "dev1@ticketing.local");
        userService.getOrCreateUser("developer2", "Dev Two", "dev2@ticketing.local");
    }

    @Test
    @DisplayName("Default Adhocs project exists and custom 3-char project can be created")
    void testProjectCreation() {
        Project adhoc = projectService.getProjectByCode("ADH");
        assertNotNull(adhoc);
        assertEquals("Adhocs", adhoc.getName());

        CreateProjectRequest req = new CreateProjectRequest();
        req.setCode("PR1");
        req.setName("Project One");
        req.setDescription("Description for Project One");
        ProjectResponse created = projectService.createProject(req);

        assertEquals("PR1", created.getCode());
        assertEquals("Project One", created.getName());
    }

    @Test
    @DisplayName("Invalid project codes (length != 3 or non-alphanumeric) are rejected")
    void testInvalidProjectCode() {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setCode("TOOLONG");
        req.setName("Invalid");
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(req));

        req.setCode("A#1");
        assertThrows(IllegalArgumentException.class, () -> projectService.createProject(req));
    }

    @Test
    @DisplayName("Ticket sequential ID generation adheres to <project-code>-00001 pattern")
    void testSequentialTicketId() {
        CreateTicketRequest req1 = new CreateTicketRequest();
        req1.setProjectCode("ADH");
        req1.setTitle("Task 1");
        TicketDetailResponse t1 = ticketService.createTicket(req1, "admin");

        CreateTicketRequest req2 = new CreateTicketRequest();
        req2.setProjectCode("ADH");
        req2.setTitle("Task 2");
        TicketDetailResponse t2 = ticketService.createTicket(req2, "admin");

        assertTrue(t1.getId().matches("ADH-\\d{5}"));
        assertTrue(t2.getId().matches("ADH-\\d{5}"));
        assertNotEquals(t1.getId(), t2.getId());
    }

    @Test
    @DisplayName("Plan scope: Ideas can be added and toggled; Promote turns active ideas to checkpoints")
    void testPlanPromotionWorkflow() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setProjectCode("ADH");
        req.setTitle("Feature Planning");
        req.setIdeas(List.of("Idea 1", "Idea 2", "Idea 3"));
        TicketDetailResponse ticket = ticketService.createTicket(req, "admin");

        assertEquals(TicketScope.PLAN, ticket.getScope());
        assertEquals(TicketPhase.PLAN, ticket.getPhase());
        assertFalse(ticket.isCompleted());
        assertNull(ticket.getAssignee());
        assertEquals(3, ticket.getIdeas().size());

        // Inactive one idea
        TicketIdea ideaToDeactivate = ticket.getIdeas().get(1);
        IdeaRequest ideaReq = new IdeaRequest();
        ideaReq.setContent(ideaToDeactivate.getContent());
        ideaReq.setActive(false);
        ticketService.updateIdea(ticket.getId(), ideaToDeactivate.getId(), ideaReq);

        // Promote ticket to Live scope
        TicketDetailResponse promoted = ticketService.promoteTicket(ticket.getId());

        assertEquals(TicketScope.LIVE, promoted.getScope());
        assertEquals(TicketPhase.PLANNED, promoted.getPhase());
        assertNotNull(promoted.getPromotedAt());
        // Only the 2 active ideas should have become checkpoints
        assertEquals(2, promoted.getCheckpoints().size());
        assertEquals("Idea 1", promoted.getCheckpoints().get(0).getTitle());
        assertFalse(promoted.getCheckpoints().get(0).isCompleted());

        // Attempting to modify ideas after promotion must fail (sealed)
        IdeaRequest newIdea = new IdeaRequest();
        newIdea.setContent("Late idea");
        assertThrows(IllegalStateException.class, () -> ticketService.addIdea(ticket.getId(), newIdea));
    }

    @Test
    @DisplayName("Live scope rules: Planned phase immutability and mandatory assignee for Execution")
    void testLivePlannedToExecutionRules() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setProjectCode("ADH");
        req.setTitle("Live Ticket");
        req.setIdeas(List.of("Checkpoint Alpha"));
        TicketDetailResponse ticket = ticketService.createTicket(req, "admin");
        ticketService.promoteTicket(ticket.getId());

        // In Planned phase: Checkpoints are immutable
        Long cpId = ticketService.getTicketDetail(ticket.getId()).getCheckpoints().get(0).getId();
        assertThrows(IllegalStateException.class, () -> ticketService.toggleCheckpoint(ticket.getId(), cpId, true));

        // Moving to Execution WITHOUT assignee must fail
        TransitionPhaseRequest toExecNoAssignee = new TransitionPhaseRequest();
        toExecNoAssignee.setPhase(TicketPhase.EXECUTION);
        assertThrows(IllegalArgumentException.class, () -> ticketService.transitionPhase(ticket.getId(), toExecNoAssignee));

        // Moving to Execution WITH assignee succeeds
        TransitionPhaseRequest toExecWithAssignee = new TransitionPhaseRequest();
        toExecWithAssignee.setPhase(TicketPhase.EXECUTION);
        toExecWithAssignee.setAssignee("developer1");
        TicketDetailResponse execTicket = ticketService.transitionPhase(ticket.getId(), toExecWithAssignee);

        assertEquals(TicketPhase.EXECUTION, execTicket.getPhase());
        assertEquals("developer1", execTicket.getAssignee());

        // In Execution: Checkpoints can be toggled
        TicketCheckpoint toggled = ticketService.toggleCheckpoint(ticket.getId(), cpId, true);
        assertTrue(toggled.isCompleted());

        // In Execution: Reassignment to another user is allowed
        TicketDetailResponse reassigned = ticketService.reassignTicket(ticket.getId(), "developer2");
        assertEquals("developer2", reassigned.getAssignee());

        // In Execution: De-assignment (empty/null assignee) must FAIL
        assertThrows(IllegalArgumentException.class, () -> ticketService.reassignTicket(ticket.getId(), ""));
        assertThrows(IllegalArgumentException.class, () -> ticketService.reassignTicket(ticket.getId(), null));
    }

    @Test
    @DisplayName("Closed tickets are immutable; cancelled tickets have completed=false (red color)")
    void testClosedAndCancelledTickets() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setProjectCode("ADH");
        req.setTitle("Live Ticket to Cancel");
        TicketDetailResponse ticket = ticketService.createTicket(req, "admin");
        ticketService.promoteTicket(ticket.getId());

        // Cancel live ticket -> Closed with completed=false
        TransitionPhaseRequest cancelReq = new TransitionPhaseRequest();
        cancelReq.setPhase(TicketPhase.CLOSED);
        cancelReq.setCompleted(false);
        TicketDetailResponse cancelled = ticketService.transitionPhase(ticket.getId(), cancelReq);

        assertEquals(TicketPhase.CLOSED, cancelled.getPhase());
        assertFalse(cancelled.isCompleted());
        assertNotNull(cancelled.getCompletedAt());

        // Closed tickets cannot be edited or transitioned
        UpdateTicketRequest updateReq = new UpdateTicketRequest();
        updateReq.setTitle("New Title");
        assertThrows(IllegalStateException.class, () -> ticketService.updateTicket(ticket.getId(), updateReq));

        // But comments CAN be added to Closed tickets
        assertDoesNotThrow(() -> ticketService.addComment(ticket.getId(), "admin", null, "Closing remarks"));
    }

    @Test
    @DisplayName("Theme preferences are persisted across user profile sessions")
    void testThemePersistence() {
        User user = userService.updateTheme("admin", "nord");
        assertEquals("nord", user.getThemePreference());

        User fetched = userService.findByUsername("admin").orElseThrow();
        assertEquals("nord", fetched.getThemePreference());
    }

    @Test
    @DisplayName("Related tickets can be linked and unlinked")
    void testRelatedTickets() {
        CreateTicketRequest r1 = new CreateTicketRequest();
        r1.setProjectCode("ADH");
        r1.setTitle("Ticket A");
        TicketDetailResponse t1 = ticketService.createTicket(r1, "admin");

        CreateTicketRequest r2 = new CreateTicketRequest();
        r2.setProjectCode("ADH");
        r2.setTitle("Ticket B");
        TicketDetailResponse t2 = ticketService.createTicket(r2, "admin");

        ticketService.linkRelatedTicket(t1.getId(), t2.getId());

        TicketDetailResponse detail1 = ticketService.getTicketDetail(t1.getId());
        assertTrue(detail1.getRelatedTickets().contains(t2.getId()));

        ticketService.unlinkRelatedTicket(t1.getId(), t2.getId());
        TicketDetailResponse detailAfter = ticketService.getTicketDetail(t1.getId());
        assertFalse(detailAfter.getRelatedTickets().contains(t2.getId()));
    }

    @Test
    @DisplayName("Normalized comment avatar: comment resolves latest user avatar dynamically at runtime")
    void testNormalizedCommentAvatarResolution() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setProjectCode("ADH");
        req.setTitle("Comment Avatar Test");
        TicketDetailResponse ticket = ticketService.createTicket(req, "admin");

        // Developer 1 posts a comment
        TicketComment comment = ticketService.addComment(ticket.getId(), "developer1", "Dev One", "First feedback");
        assertNotNull(comment);
        assertEquals("/api/photos/default/avatar-1.svg", comment.getAuthorAvatarUrl());

        // Developer 1 updates avatar
        userService.updateAvatar("developer1", "/api/photos/custom/avatar-custom.png");

        // Fetch ticket details again - comment avatar must dynamically reflect new avatar
        TicketDetailResponse updatedTicket = ticketService.getTicketDetail(ticket.getId());
        assertEquals(1, updatedTicket.getComments().size());
        assertEquals("/api/photos/custom/avatar-custom.png", updatedTicket.getComments().get(0).getAuthorAvatarUrl());
    }

    @Test
    @DisplayName("Activity tracking: tracks all events throughout ticket existence, including after closed")
    void testActivityTrackingThroughoutLifecycleAndAfterClosed() {
        CreateTicketRequest req = new CreateTicketRequest();
        req.setProjectCode("ADH");
        req.setTitle("Activity Tracking Test");
        req.setIdeas(List.of("Idea 1", "Idea 2"));
        TicketDetailResponse ticket = ticketService.createTicket(req, "admin");

        // 1. Creation activity recorded
        List<TicketActivityResponse> activities = ticketService.getTicketActivities(ticket.getId());
        assertFalse(activities.isEmpty());
        assertEquals(TicketActivityType.TICKET_CREATED, activities.get(activities.size() - 1).getActivityType());
        assertEquals("admin", activities.get(activities.size() - 1).getUsername());

        // 2. Add Idea activity
        IdeaRequest ideaReq = new IdeaRequest();
        ideaReq.setContent("Idea 3");
        ideaReq.setActive(true);
        ticketService.addIdea(ticket.getId(), ideaReq);

        // 3. Promote ticket activity
        ticketService.promoteTicket(ticket.getId());

        // 4. Transition to Execution activity
        TransitionPhaseRequest toExec = new TransitionPhaseRequest();
        toExec.setPhase(TicketPhase.EXECUTION);
        toExec.setAssignee("alex");
        ticketService.transitionPhase(ticket.getId(), toExec);

        // 5. Reassign ticket activity
        ticketService.reassignTicket(ticket.getId(), "sarah");

        // 6. Toggle checkpoint activity
        TicketDetailResponse liveDetail = ticketService.getTicketDetail(ticket.getId());
        assertFalse(liveDetail.getCheckpoints().isEmpty());
        ticketService.toggleCheckpoint(ticket.getId(), liveDetail.getCheckpoints().get(0).getId(), true);

        // 7. Transition to CLOSED activity
        TransitionPhaseRequest toClose = new TransitionPhaseRequest();
        toClose.setPhase(TicketPhase.CLOSED);
        toClose.setCompleted(true);
        ticketService.transitionPhase(ticket.getId(), toClose);

        // 8. Add comment on CLOSED ticket - MUST STILL TRACK ACTIVITY!
        ticketService.addComment(ticket.getId(), "alex", "Alex", "Post-closure comment check");

        // Verify full timeline in ticket detail
        TicketDetailResponse finalDetail = ticketService.getTicketDetail(ticket.getId());
        List<TicketActivityResponse> finalActivities = finalDetail.getActivities();
        assertNotNull(finalActivities);
        assertTrue(finalActivities.size() >= 8);

        // The most recent activity should be the comment added on the closed ticket
        assertEquals(TicketActivityType.COMMENT_ADDED, finalActivities.get(0).getActivityType());
        assertEquals("alex", finalActivities.get(0).getUsername());

        // Check for presence of all key activity types
        List<TicketActivityType> types = finalActivities.stream().map(TicketActivityResponse::getActivityType).toList();
        assertTrue(types.contains(TicketActivityType.TICKET_CREATED));
        assertTrue(types.contains(TicketActivityType.IDEA_ADDED));
        assertTrue(types.contains(TicketActivityType.TICKET_PROMOTED));
        assertTrue(types.contains(TicketActivityType.PHASE_TRANSITIONED));
        assertTrue(types.contains(TicketActivityType.TICKET_REASSIGNED));
        assertTrue(types.contains(TicketActivityType.CHECKPOINT_TOGGLED));
        assertTrue(types.contains(TicketActivityType.TICKET_COMPLETED));
        assertTrue(types.contains(TicketActivityType.COMMENT_ADDED));
    }

    @Test
    @DisplayName("Photo upload within size limit succeeds")
    void testPhotoUploadWithinLimit() throws Exception {
        assertEquals(2L, photoService.getMaxSizeMb());
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-avatar.png",
                "image/png",
                new byte[1024 * 100] // 100 KB
        );
        PredefinedPhoto photo = photoService.uploadCustomPhoto(file, "Test Avatar", "USER");
        assertNotNull(photo);
        assertTrue(photo.isCustom());
        assertTrue(photo.getUrl().startsWith("/api/photos/custom/"));
    }

    @Test
    @DisplayName("Photo upload exceeding configured limit is rejected with IllegalArgumentException")
    void testPhotoUploadExceedingLimit() {
        assertEquals(2L, photoService.getMaxSizeMb());
        // 2MB + 1 byte
        byte[] oversizedData = new byte[(int) (2 * 1024 * 1024 + 1)];
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "large-avatar.png",
                "image/png",
                oversizedData
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            photoService.uploadCustomPhoto(file, "Large Avatar", "USER");
        });
        assertTrue(ex.getMessage().contains("Photo size exceeds maximum allowed size of 2 MB"));
    }

    @Test
    @DisplayName("Photo config endpoint returns configured max size in MB")
    void testPhotoConfigEndpoint() {
        var response = photoController.getConfig();
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().get("maxSizeMb"));
    }
}
