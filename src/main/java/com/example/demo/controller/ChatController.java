package com.example.demo.controller;


import com.example.demo.domain.MessageRoom;
import com.example.demo.dto.response.message.MessageResponse;

import com.example.demo.dto.response.message.MessageRoomDTO;
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
    public ResponseEntity<List<MessageRoomDTO>> getMyRooms() {
        List<MessageRoomDTO> rooms = messageService.getMyRooms();
        return ResponseEntity.ok(rooms);
    }
    // Đếm só lượng người gửi tin nhắn
    @GetMapping("/chat/unread-room-count")
    @ApiMessage("Get number of rooms with unread messages")
    public ResponseEntity<Integer> getUnreadRoomCount() {
        Integer count = messageService.getUnreadRoomCount();
        return ResponseEntity.ok(count);
    }
    // Đánh dấu dã đọc khi click vào icon message
    @PutMapping("/chat/reset-unread")
    @ApiMessage("Reset all unread counts")
    public ResponseEntity<Void> resetAllUnreadCounts() {
        messageService.resetAllUnreadCounts();
        return ResponseEntity.ok().build();
    }
    // ✅ API mới: Đánh dấu phòng đã đọc
    @PutMapping("/chat/room/{roomId}/mark-read")
    @ApiMessage("Mark room as read")
    public ResponseEntity<Void> markRoomAsRead(@PathVariable UUID roomId) {
        messageService.markRoomAsRead(roomId);
        return ResponseEntity.ok().build();
    }
}
