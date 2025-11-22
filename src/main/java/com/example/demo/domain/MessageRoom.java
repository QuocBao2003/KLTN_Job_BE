package com.example.demo.domain;


import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Data
@Table(name = "message_room")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class MessageRoom {
    @Id
    @GeneratedValue(generator = "UUID", strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "candidate_id")

    private User candidate;

    @ManyToOne
    @JoinColumn(name = "employer_id")

    private User employer;

    @ManyToOne
    @JoinColumn(name = "job_id")
    private Job job;

    @CreatedDate
    private LocalDateTime createdDate;


    @Column(name = "candidate_unread_count")
    private Integer candidateUnreadCount = 0;

    @Column(name = "employer_unread_count")
    private Integer employerUnreadCount = 0;

    @Column(name = "last_message")
    private String lastMessage;

    @Column(name = "last_message_time")
    private LocalDateTime lastMessageTime;

    @Column(name = "last_sender_id")
    private Long lastSenderId;

    @OneToMany(mappedBy = "messageRoom", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageContent> messages;
}
