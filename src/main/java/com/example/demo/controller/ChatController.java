package com.example.demo.controller;


import com.example.demo.domain.MessageRoom;
import com.example.demo.dto.response.MessageResponse;

import com.example.demo.service.MessageService;
import com.example.demo.util.annotation.ApiMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ChatController {

    private final MessageService messageService;

    /**
     * Tạo hoặc lấy phòng chat
     * - Candidate: chỉ cần truyền jobId
     * - HR: cần truyền jobId + otherUserId (candidateId)
     *
     * @param jobId ID của công việc
     * @param otherUserId ID của user khác (bắt buộc nếu HR gọi)
     * @return MessageRoom
     */
    @PostMapping("/chat/room")
    @ApiMessage("Create or get chat room")
    public ResponseEntity<MessageRoom> createOrGetRoom(
            @RequestParam Long jobId,
            @RequestParam(required = false) Long otherUserId) {

        MessageRoom room = messageService.getOrCreateRoomByJob(jobId, otherUserId);
        return ResponseEntity.status(HttpStatus.CREATED).body(room);
    }

    /**
     * Lấy danh sách tin nhắn trong phòng
     *
     * @param roomId UUID của phòng chat
     * @return Danh sách tin nhắn
     */
    @GetMapping("/chat/room/{roomId}/messages")
    @ApiMessage("Get messages in room")
    public ResponseEntity<List<MessageResponse>> getMessages(@PathVariable UUID roomId) {
        List<MessageResponse> messages = messageService.getMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * Lấy danh sách tất cả phòng chat của user hiện tại
     * - Candidate: lấy phòng mà họ là candidate
     * - HR: lấy phòng mà họ là employer
     *
     * @return Danh sách phòng chat
     */
    @GetMapping("/chat/rooms")
    @ApiMessage("Get all my chat rooms")
    public ResponseEntity<List<MessageRoom>> getMyRooms() {
        List<MessageRoom> rooms = messageService.getMyRooms();
        return ResponseEntity.ok(rooms);
    }
}
