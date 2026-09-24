package com.ticketing;

import com.ticketing.dto.*;
import com.ticketing.model.*;
import com.ticketing.service.ProjectService;
import com.ticketing.service.TicketService;
import com.ticketing.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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
}
