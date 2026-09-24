package com.ticketing.repository;

import com.ticketing.model.TicketIdea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketIdeaRepository extends JpaRepository<TicketIdea, Long> {
    List<TicketIdea> findByTicketIdOrderByOrderIndexAsc(String ticketId);
    void deleteByTicketId(String ticketId);
}
