package com.ticketing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "related_tickets", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"ticket_id", "related_ticket_id"})
})
public class RelatedTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, length = 32)
    private String ticketId;

    @Column(name = "related_ticket_id", nullable = false, length = 32)
    private String relatedTicketId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public RelatedTicket() {}

    public RelatedTicket(String ticketId, String relatedTicketId) {
        this.ticketId = ticketId;
        this.relatedTicketId = relatedTicketId;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getRelatedTicketId() { return relatedTicketId; }
    public void setRelatedTicketId(String relatedTicketId) { this.relatedTicketId = relatedTicketId; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
