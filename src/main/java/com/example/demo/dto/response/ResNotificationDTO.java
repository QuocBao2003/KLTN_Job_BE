package com.example.demo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ResNotificationDTO {
    private Long id;
    private String title;
    private String description;
    private String type;
    private boolean isRead;
    private Long relatedEntityId;
    private String navigationUrl;
    private Instant createdAt;


}
