package com.ticketing.repository;

import com.ticketing.model.TicketActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketActivityRepository extends JpaRepository<TicketActivity, Long> {
    List<TicketActivity> findByTicketIdOrderByTimestampDesc(String ticketId);
    List<TicketActivity> findByTicketIdOrderByTimestampAsc(String ticketId);
    long countByTicketId(String ticketId);
}
