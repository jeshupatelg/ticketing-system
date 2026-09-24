package com.ticketing.model;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "ticket_checkpoints")
public class TicketCheckpoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, length = 32)
    private String ticketId;

    @Column(nullable = false, length = 1000)
    private String title;

    @Column(nullable = false)
    private boolean completed = false;

    @Column(name = "order_index", nullable = false)
    private int orderIndex = 0;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public TicketCheckpoint() {}

    public TicketCheckpoint(String ticketId, String title, boolean completed, int orderIndex) {
        this.ticketId = ticketId;
        this.title = title;
        this.completed = completed;
        this.orderIndex = orderIndex;
        this.createdAt = Instant.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
