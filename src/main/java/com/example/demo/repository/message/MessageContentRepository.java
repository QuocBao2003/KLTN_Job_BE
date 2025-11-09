package com.example.demo.repository.message;


import com.example.demo.domain.MessageContent;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageContentRepository extends JpaRepository<MessageContent, UUID> {

    @Query("""
        SELECT m FROM MessageContent m
        JOIN FETCH m.sender s
        JOIN FETCH m.messageRoom r
        WHERE r.id = :roomId
        ORDER BY m.dateSent ASC
    """)
    List<MessageContent> findMessagesWithSenderByRoomId(@Param("roomId") UUID roomId);
}
