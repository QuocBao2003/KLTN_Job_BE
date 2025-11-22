package com.example.demo.domain;


import com.example.demo.util.Enum.NotificationType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "notifications")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String discription;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;
    private boolean isRead;
    @Column(nullable = false)
    private boolean isViewed = false;
    private Instant readAt;
    private long relatedEntityId;
    private String navigationUrl;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    private Instant createdAt;
    private Instant updatedAt;

    @PrePersist
    public void handleBeforeCreate() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void handleBeforeUpdate() {
        this.updatedAt = Instant.now();
    }
}
