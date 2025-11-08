package com.example.demo.controller;

import com.example.demo.dto.request.ChatRequest;
import com.example.demo.dto.response.chatai.ChatResponse;
import com.example.demo.service.ChatService;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ChaiAiController {
    private final ChatService chatService;

    @PostMapping("/messageAi")
    public ResponseEntity<ChatResponse> chat(@RequestBody ChatRequest chatRequest){
        String conversationId = getCurrentUserId();
        ChatResponse response = chatService.chat(chatRequest.getMessage(), conversationId);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/fileAi")
    public ResponseEntity<ChatResponse> fileAi(@RequestParam("file") MultipartFile file,@RequestParam(value = "message",defaultValue = "Tìm công việc phù hợp với CV của tôi")
                                               String message){
        String conversationId = getCurrentUserId();
        ChatResponse response = chatService.chatWithFile(file, message, conversationId);
        return ResponseEntity.ok(response);
    }

    private String getCurrentUserId() {
        return SecurityUtil.getCurrentUserLogin()
                .orElse("anonymous_" + System.currentTimeMillis());
    }
}
