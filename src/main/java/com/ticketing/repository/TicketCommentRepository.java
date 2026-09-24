package com.ticketing.repository;

import com.ticketing.model.TicketComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketCommentRepository extends JpaRepository<TicketComment, Long> {
    List<TicketComment> findByTicketIdOrderByCreatedAtAsc(String ticketId);
    void deleteByTicketId(String ticketId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.data.jpa.repository.Query("UPDATE TicketComment c SET c.authorAvatarUrl = :avatarUrl WHERE c.authorUsername = :username OR c.author = :username OR c.author = :name")
    void updateAvatarByAuthor(@org.springframework.data.repository.query.Param("username") String username,
                              @org.springframework.data.repository.query.Param("name") String name,
                              @org.springframework.data.repository.query.Param("avatarUrl") String avatarUrl);
}
