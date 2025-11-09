package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.MessageResponse;
import com.example.demo.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload MessageRequest request, Principal principal,  SimpMessageHeaderAccessor headerAccessor) {
        if (principal == null) {
            log.error("❌ Principal is null");
            throw new RuntimeException("Bạn chưa đăng nhập");
        }

        log.info("📩 Principal name: {}", principal.getName());
        log.info("📩 Principal class: {}", principal.getClass().getName());

        User user = null;

        // ✅ Cách 1: Lấy từ session attributes (KHUYÊN DÙNG)
        if (headerAccessor != null) {
            user = (User) headerAccessor.getSessionAttributes().get("user");
            if (user != null) {
                log.info("✅ Lấy user từ session attributes: {}", user.getEmail());
            }
        }

        // ✅ Gửi message và lưu DB
        MessageResponse response = messageService.sendMessage(request, user);

        // ✅ Gửi lại cho 2 bên trong room
        String roomId = request.getMessageRoomId().toString();
        simpMessagingTemplate.convertAndSend("/topic/room/" + roomId, response);
        log.info("✅ Tin nhắn đã được gửi đến /topic/room/{}", roomId);
    }
}
