package com.example.demo.dto.response.message;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRoomDTO {
    private UUID id;

    private Long jobId;
    private String jobName;
    private String companyName;

    private Long otherUserId;
    private String otherUserName;
    private String otherUserEmail;
    private String otherUserAvatar;

    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long lastSenderId;

    private Integer unreadCount;

    private LocalDateTime createdDate;

}
