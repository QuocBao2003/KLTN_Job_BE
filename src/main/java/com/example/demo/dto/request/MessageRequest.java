package com.example.demo.dto.request;

import com.example.demo.util.Enum.MessageType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private String content;
    private MessageType messageType;
    private UUID messageRoomId;

}
