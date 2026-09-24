package com.ticketing.repository;

import com.ticketing.model.Ticket;
import com.ticketing.model.TicketPhase;
import com.ticketing.model.TicketScope;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, String> {

    List<Ticket> findByProjectCode(String projectCode);

    List<Ticket> findByProjectCodeAndScope(String projectCode, TicketScope scope);

    long countByScope(TicketScope scope);

    long countByPhase(TicketPhase phase);

    long countByCompleted(boolean completed);

    long countByProjectCode(String projectCode);

    @Query("SELECT t FROM Ticket t WHERE t.projectCode = :projectCode AND t.scope = 'LIVE' " +
           "AND (t.phase != 'CLOSED' OR t.completedAt IS NULL OR t.completedAt >= :oneWeekAgo)")
    List<Ticket> findLiveTicketsDefault(String projectCode, Instant oneWeekAgo);

    @Query("SELECT DISTINCT t.tags FROM Ticket t WHERE t.tags IS NOT NULL AND t.tags != ''")
    List<String> findAllTagsRaw();
}
