package com.example.demo.service;


import com.example.demo.domain.Job;
import com.example.demo.domain.MessageContent;
import com.example.demo.domain.MessageRoom;
import com.example.demo.domain.User;
import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.message.MessageResponse;
import com.example.demo.dto.response.message.MessageRoomDTO;
import com.example.demo.repository.JobRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.message.MessageContentRepository;
import com.example.demo.repository.message.MessageRoomRepository;
import com.example.demo.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRoomRepository messageRoomRepository;
    private final MessageContentRepository messageContentRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    public MessageRoom getOrCreateRoomByJob(Long jobId, Long otherUserId) {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        String currentRoleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        User candidateUser;
        User employerUser;

        if ("HR".equalsIgnoreCase(currentRoleName)) {
            employerUser = currentUser;
            if (otherUserId == null) {
                throw new RuntimeException("Không tìm thấy ứng viên để nhắn tin");
            }
            candidateUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("Ứng viên không tồn tại"));
        } else {
            candidateUser = currentUser;
            employerUser = userRepository.findByCompanyIdAndRoleName(
                            job.getCompany().getId(), "HR")
                    .orElseThrow(() -> new RuntimeException(
                            "Công ty này chưa có HR. Vui lòng liên hệ admin"));
        }

        return messageRoomRepository
                .findByCandidateIdAndEmployerIdAndJobId(
                        candidateUser.getId(), employerUser.getId(), jobId)
                .orElseGet(() -> {
                    MessageRoom room = MessageRoom.builder()
                            .candidate(candidateUser)
                            .employer(employerUser)
                            .job(job)
                            .candidateUnreadCount(0)
                            .employerUnreadCount(0)
                            .build();
                    return messageRoomRepository.save(room);
                });
    }

    public MessageResponse sendMessage(MessageRequest request, User currentUser) {
        MessageRoom room = messageRoomRepository.findById(request.getMessageRoomId())
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        if (!room.getCandidate().getId().equals(currentUser.getId())
                && !room.getEmployer().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Bạn không có quyền gửi tin nhắn trong phòng này");
        }

        MessageContent message = MessageContent.builder()
                .content(request.getContent())
                .messageType(request.getMessageType())
                .messageRoom(room)
                .sender(currentUser)
                .build();

        message = messageContentRepository.save(message);

        room.setLastMessage(request.getContent());
        room.setLastMessageTime(message.getDateSent());
        room.setLastSenderId(currentUser.getId());

        // Nếu sender là candidate → tăng employerUnreadCount
        if (currentUser.getId().equals(room.getCandidate().getId())) {
            room.setEmployerUnreadCount(room.getEmployerUnreadCount() + 1);
        } else {
            // Nếu sender là employer → tăng candidateUnreadCount
            room.setCandidateUnreadCount(room.getCandidateUnreadCount() + 1);
        }

        messageRoomRepository.save(room);

        return MessageResponse.builder()
                .id(message.getId())
                .content(message.getContent())
                .dateSent(message.getDateSent())
                .senderId(currentUser.getId())
                .senderUsername(currentUser.getEmail())
                .senderAvatarUrl(currentUser.getAvatarUrl())
                .build();
    }

    public List<MessageResponse> getMessages(UUID roomId) {
        List<MessageContent> messages = messageContentRepository.findMessagesWithSenderByRoomId(roomId);

        return messages.stream()
                .map(m -> MessageResponse.builder()
                        .id(m.getId())
                        .content(m.getContent())
                        .dateSent(m.getDateSent())
                        .senderId(m.getSender().getId())
                        .senderUsername(m.getSender().getEmail())
                        .senderAvatarUrl(m.getSender().getAvatarUrl())
                        .build())
                .toList();
    }
    public List<MessageRoomDTO> getMyRooms() {
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        String roleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        if (roleName == null) {
            throw new RuntimeException("User chưa có role");
        }

        List<MessageRoom> rooms;
        if ("HR".equalsIgnoreCase(roleName)) {
            rooms = messageRoomRepository.findByEmployerIdOrderByLastMessageTimeDesc(currentUser.getId());
        } else {
            rooms = messageRoomRepository.findByCandidateIdOrderByLastMessageTimeDesc(currentUser.getId());
        }

        // Map sang DTO với thông tin đầy đủ
        return rooms.stream()
                .map(room -> {
                    boolean isHR = "HR".equalsIgnoreCase(roleName);
                    User otherUser = isHR ? room.getCandidate() : room.getEmployer();
                    int unreadCount = isHR ? room.getEmployerUnreadCount() : room.getCandidateUnreadCount();

                    return MessageRoomDTO.builder()
                            .id(room.getId())
                            .jobId(room.getJob().getId())
                            .jobName(room.getJob().getName())
                            .companyName(room.getJob().getCompany().getName())
                            .otherUserId(otherUser.getId())
                            .otherUserName(otherUser.getName())
                            .otherUserEmail(otherUser.getEmail())
                            .otherUserAvatar(otherUser.getAvatarUrl())
                            .lastMessage(room.getLastMessage())
                            .lastMessageTime(room.getLastMessageTime())
                            .lastSenderId(room.getLastSenderId())
                            .unreadCount(unreadCount)
                            .createdDate(room.getCreatedDate())
                            .build();
                })
                .toList();
    }
    // ✅ Đếm số lượng PHÒNG có tin nhắn chưa đọc (số người gửi)
    public Integer getUnreadRoomCount() {
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        String roleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        if (!"HR".equalsIgnoreCase(roleName)) {
            // User (candidate) - đếm số phòng có employerUnreadCount > 0
            return messageRoomRepository.countRoomsWithUnreadForCandidate(currentUser.getId());
        } else {
            // HR (employer) - đếm số phòng có candidateUnreadCount > 0
            return messageRoomRepository.countRoomsWithUnreadForEmployer(currentUser.getId());
        }
    }
    @Transactional
    public void resetAllUnreadCounts() {
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        String roleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        if (!"HR".equalsIgnoreCase(roleName)) {
            // User (candidate) → reset tất cả candidateUnreadCount về 0
            messageRoomRepository.resetCandidateUnreadCount(currentUser.getId());
        } else {
            // HR (employer) → reset tất cả employerUnreadCount về 0
            messageRoomRepository.resetEmployerUnreadCount(currentUser.getId());
        }
    }

    // ✅ Đánh dấu đã đọc khi vào 1 room cụ thể (giữ lại nếu cần)
    @Transactional
    public void markRoomAsRead(UUID roomId) {
        String currentEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentEmail)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        MessageRoom room = messageRoomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Phòng chat không tồn tại"));

        String roleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        if (!"HR".equalsIgnoreCase(roleName)) {
            room.setCandidateUnreadCount(0);
        } else {
            room.setEmployerUnreadCount(0);
        }

        messageRoomRepository.save(room);
    }
}
