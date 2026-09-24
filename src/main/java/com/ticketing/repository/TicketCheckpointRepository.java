package com.ticketing.repository;

import com.ticketing.model.TicketCheckpoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCheckpointRepository extends JpaRepository<TicketCheckpoint, Long> {
    List<TicketCheckpoint> findByTicketIdOrderByOrderIndexAsc(String ticketId);
    void deleteByTicketId(String ticketId);
}
