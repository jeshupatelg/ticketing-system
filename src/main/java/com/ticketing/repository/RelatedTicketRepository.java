package com.ticketing.repository;

import com.ticketing.model.RelatedTicket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RelatedTicketRepository extends JpaRepository<RelatedTicket, Long> {
    List<RelatedTicket> findByTicketId(String ticketId);
    Optional<RelatedTicket> findByTicketIdAndRelatedTicketId(String ticketId, String relatedTicketId);
    void deleteByTicketId(String ticketId);
    void deleteByTicketIdAndRelatedTicketId(String ticketId, String relatedTicketId);
}
