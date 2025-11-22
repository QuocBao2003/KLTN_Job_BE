package com.example.demo.service;


import com.example.demo.domain.*;
import com.example.demo.dto.response.ResNotificationDTO;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.ResumeRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.util.Enum.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate simpMessagingTemplate;
    private final ResumeRepository resumeRepository;

//    Tạo thông báo khi Job được tạo ( ADMIN)
    public void notificationJobCreate(Job job){
        List<User> superAdmins= userRepository.findByRole_Name("SUPER_ADMIN");
        for(User admin:superAdmins){
            Notification notification=new Notification();
            notification.setTitle("Công việc mới cần duyệt");
            notification.setDiscription("Công ty \"" + job.getCompany().getName() + "đã tạo công việc tên \"" +job.getName() +" cần được duyệt");
            notification.setType(NotificationType.JOB_PENDING_APPROVAL);
            notification.setRelatedEntityId(job.getId());
            notification.setNavigationUrl("/admin/job");
            notification.setUser(admin);
            Notification saved = notificationRepository.save(notification);
            sendNotificationToUser(admin.getEmail(),saved);
        }
    }
//    Thong bao khi job dc approve
    public void notifyJobApproval(Job job){
        User hr = job.getCompany().getUsers().stream()
                .filter(u -> "HR".equals(u.getRole().getName()))
                .findFirst().orElse(null);
        if(hr!=null){
            Notification notification=new Notification();
            notification.setTitle("Công việc đã được duyệt");
            notification.setDiscription("Job " + job.getName()+" đã được phê duyệt thành công");
            notification.setType(NotificationType.JOB_APPROVED);
            notification.setRelatedEntityId(job.getId());
            notification.setNavigationUrl("/admin/job");
            notification.setUser(hr);
            Notification saved = notificationRepository.save(notification);
            sendNotificationToUser(hr.getEmail(),saved);
        }
    }
    public void notifyJobReject(Job job){
        User hr = job.getCompany().getUsers().stream()
                .filter(u -> "HR".equals(u.getRole().getName()))
                .findFirst().orElse(null);
        if(hr!=null){
            Notification notification=new Notification();
            notification.setTitle("Công việc mới không được duyệt");
            notification.setDiscription("Job " + job.getName()+" đã được phê duyệt thất bại");
            notification.setType(NotificationType.JOB_REJECTED);
            notification.setRelatedEntityId(job.getId());
            notification.setNavigationUrl("/admin/job");
            notification.setUser(hr);
            Notification saved = notificationRepository.save(notification);
            sendNotificationToUser(hr.getEmail(),saved);
        }
    }
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyResumeCreatedAsync(Long resumeId) {
        try {
            log.info("🔔 [ASYNC] Bắt đầu gửi notification cho Resume ID: {}", resumeId);
            notifyResumeCreated(resumeId);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Lỗi khi gửi notification create resume: {}", e.getMessage(), e);
        }
    }

    // ✅ Method ASYNC cho Resume Approved
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyResumeApprovedAsync(Long resumeId) {
        try {
            log.info("🔔 [ASYNC] Bắt đầu gửi notification approved cho Resume ID: {}", resumeId);
            notifyResumeApproved(resumeId);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Lỗi khi gửi notification approved: {}", e.getMessage(), e);
        }
    }

    // ✅ Method ASYNC cho Resume Rejected
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void notifyResumeRejectedAsync(Long resumeId) {
        try {
            log.info("🔔 [ASYNC] Bắt đầu gửi notification rejected cho Resume ID: {}", resumeId);
            notifyResumeRejected(resumeId);
        } catch (Exception e) {
            log.error("❌ [ASYNC] Lỗi khi gửi notification rejected: {}", e.getMessage(), e);
        }
    }
    public void notifyResumeCreated(Long resumeId) {
        log.info("🚀 [START] Tạo notification cho Resume ID: {}", resumeId);

        Resume resume = resumeRepository.findWithRelationsById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        log.info("✅ Resume found: ID={}, Email={}", resume.getId(), resume.getEmail());

        Job job = resume.getJob();
        if (job == null) {
            log.warn("⚠️ Job null cho Resume ID: {}", resumeId);
            return;
        }
        log.info("✅ Job found: ID={}, Name={}", job.getId(), job.getName());

        Company company = job.getCompany();
        if (company == null) {
            log.warn("⚠️ Company null cho Job ID: {}", job.getId());
            return;
        }
        log.info("✅ Company found: ID={}, Name={}", company.getId(), company.getName());

        User hr = company.getHr();

        if (hr == null) {
            log.warn("⚠️ HR null cho Company: {} (ID: {})", company.getName(), company.getId());
            return;
        }

        log.info("✅ HR found: ID={}, Name={}, Email={}, Role={}",
                hr.getId(),
                hr.getName(),
                hr.getEmail(),
                hr.getRole() != null ? hr.getRole().getName() : "NULL");

        if (hr.getRole() == null || !"HR".equals(hr.getRole().getName())) {
            log.warn("⚠️ User {} không phải là HR (Role: {})",
                    hr.getEmail(),
                    hr.getRole() != null ? hr.getRole().getName() : "NULL");
            return;
        }

        Notification notification = new Notification();
        notification.setTitle("Đơn ứng tuyển mới");
        notification.setDiscription(
                resume.getUser().getName() +
                        " đã ứng tuyển vào vị trí \"" + job.getName() + "\""
        );
        notification.setType(NotificationType.RESUME_PENDING);
        notification.setRelatedEntityId(resume.getId());
        notification.setNavigationUrl("/admin/resume");
        notification.setUser(hr);

        Notification saved = notificationRepository.save(notification);
        log.info("💾 Đã lưu notification: ID={}, Title={}", saved.getId(), saved.getTitle());

        sendNotificationToUser(hr.getEmail(), saved);
        log.info("✅ [SUCCESS] Hoàn thành tạo notification cho Resume ID: {}", resumeId);
    }

    @Transactional
    public void notifyResumeApproved(Long resumeId) {
        Resume resume = resumeRepository.findWithRelationsById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        if (resume.getJob() == null || resume.getJob().getCompany() == null) {
            log.warn("⚠️ Job hoặc Company null cho Resume ID: {}", resumeId);
            return;
        }

        Notification notification = new Notification();
        notification.setTitle("Đơn ứng tuyển được chấp nhận");
        notification.setDiscription(
                "Chúc mừng! Đơn ứng tuyển của bạn cho vị trí \"" +
                        resume.getJob().getName() + "\" tại " +
                        resume.getJob().getCompany().getName() +
                        " đã được chấp nhận. HR sẽ liên hệ với bạn sớm"
        );
        notification.setType(NotificationType.RESUME_APPROVED);
        notification.setRelatedEntityId(resume.getJob().getId());
        notification.setNavigationUrl("/jobapply");
        notification.setUser(resume.getUser());

        Notification saved = notificationRepository.save(notification);
        sendNotificationToUser(resume.getUser().getEmail(), saved);
    }

    @Transactional
    public void notifyResumeRejected(Long resumeId) {
        Resume resume = resumeRepository.findWithRelationsById(resumeId)
                .orElseThrow(() -> new RuntimeException("Resume not found: " + resumeId));

        if (resume.getJob() == null || resume.getJob().getCompany() == null) {
            log.warn("⚠️ Job hoặc Company null cho Resume ID: {}", resumeId);
            return;
        }

        Notification notification = new Notification();
        notification.setTitle("Đơn ứng tuyển bị từ chối");
        notification.setDiscription(
                "Rất tiếc, đơn ứng tuyển của bạn cho vị trí \"" +
                        resume.getJob().getName() + "\" tại " +
                        resume.getJob().getCompany().getName() +
                        " chưa phù hợp lần này. Đừng nản chí và tiếp tục tìm kiếm cơ hội mới!"
        );
        notification.setType(NotificationType.RESUME_REJECTED);
        notification.setRelatedEntityId(resume.getJob().getId());
        notification.setNavigationUrl("/jobapply");
        notification.setUser(resume.getUser());

        Notification saved = notificationRepository.save(notification);
        sendNotificationToUser(resume.getUser().getEmail(), saved);
    }
    // Gửi notification qua WebSocket
    private void sendNotificationToUser(String userEmail, Notification notification) {
        try {
            ResNotificationDTO dto = convertToDTO(notification);
            simpMessagingTemplate.convertAndSendToUser(
                    userEmail,
                    "/topic/notifications",
                    dto
            );
            log.info("✅ Đã gửi notification cho user: {}", userEmail);
        } catch (Exception e) {
            log.error("❌ Lỗi khi gửi notification: {}", e.getMessage());
        }
    }

    public List<ResNotificationDTO> getAllNotifications(Long userId) {
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Notification> notifications = notificationRepository
                .findRecentNotificationsByUserId(userId, sevenDaysAgo);
        return notifications.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // THAY ĐỔI: Đếm số thông báo CHƯA XEM (viewed)
    public Long countUnviewedNotifications(Long userId) {
        return notificationRepository.countByUserIdAndIsViewedFalse(userId);
    }

    // THAY ĐỔI: Đánh dấu tất cả là đã XEM (viewed) - Gọi khi user click vào icon thông báo
    @Transactional
    public void markAllAsViewed(Long userId) {
        notificationRepository.markAllAsViewedByUserId(userId);
    }

    // THAY ĐỔI: Đánh dấu 1 thông báo là đã ĐỌC (read) - Gọi khi user click vào thông báo cụ thể
    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.isRead()) {
            notification.setRead(true);
            notification.setReadAt(Instant.now());
            notificationRepository.save(notification);
        }
    }

    //  Xóa thông báo đã đọc quá 7 ngày (có thể chạy bằng scheduled job)
    @Transactional
    @Scheduled(cron = "0 0 2 * * ?") // Chạy lúc 2h sáng mỗi ngày
    public void cleanupOldNotifications() {
        Instant sevenDaysAgo = Instant.now().minus(7, ChronoUnit.DAYS);
        List<Notification> oldNotifications = notificationRepository
                .findByUserIdAndIsReadTrueAndReadAtBefore(null, sevenDaysAgo);

        if (!oldNotifications.isEmpty()) {
            notificationRepository.deleteAll(oldNotifications);
            log.info("🗑️ Đã xóa {} thông báo cũ", oldNotifications.size());
        }
    }


    private ResNotificationDTO convertToDTO(Notification notification) {
        ResNotificationDTO dto = new ResNotificationDTO();
        dto.setId(notification.getId());
        dto.setTitle(notification.getTitle());
        dto.setDescription(notification.getDiscription());
        dto.setType(notification.getType().toString());
        dto.setRead(notification.isRead());
        dto.setRelatedEntityId(notification.getRelatedEntityId());
        dto.setNavigationUrl(notification.getNavigationUrl());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
}
