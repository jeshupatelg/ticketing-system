package com.ticketing.repository;

import com.ticketing.model.TicketAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketAttachmentRepository extends JpaRepository<TicketAttachment, Long> {
    List<TicketAttachment> findByTicketIdOrderByUploadedAtAsc(String ticketId);
    long countByTicketId(String ticketId);
    void deleteByTicketId(String ticketId);
}
