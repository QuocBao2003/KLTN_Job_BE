package com.example.demo.controller;

import com.example.demo.domain.User;
import com.example.demo.dto.response.ResNotificationDTO;
import com.example.demo.service.NotificationService;
import com.example.demo.service.UserService;
import com.example.demo.util.SecurityUtil;
import com.example.demo.util.annotation.ApiMessage;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final UserService userService;

    public NotificationController(NotificationService notificationService, UserService userService) {
        this.notificationService = notificationService;
        this.userService = userService;
    }


    @GetMapping("/all")
    @ApiMessage("Get all recent notifications")
    public ResponseEntity<List<ResNotificationDTO>> getAllNotifications() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<ResNotificationDTO> notifications = notificationService.getAllNotifications(user.getId());
        return ResponseEntity.ok(notifications);
    }

    // Đếm thông báo CHƯA XEM (viewed)
    @GetMapping("/count")
    @ApiMessage("Count unviewed notifications")
    public ResponseEntity<Map<String, Long>> countUnviewedNotifications() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long count = notificationService.countUnviewedNotifications(user.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Đánh dấu tất cả là đã XEM
    @PutMapping("/mark-all-viewed")
    @ApiMessage("Mark all notifications as viewed")
    public ResponseEntity<Void> markAllAsViewed() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("User not found"));

        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        notificationService.markAllAsViewed(user.getId());
        return ResponseEntity.ok().build();
    }

    // Đánh dấu 1 thông báo là đã ĐỌC
    @PutMapping("/{id}/read")
    @ApiMessage("Mark notification as read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long id) {
        notificationService.markAsRead(id);
        return ResponseEntity.ok().build();
    }
}