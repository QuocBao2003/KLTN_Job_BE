package com.example.demo.service;


import com.example.demo.domain.Job;
import com.example.demo.domain.MessageContent;
import com.example.demo.domain.MessageRoom;
import com.example.demo.domain.User;
import com.example.demo.dto.request.MessageRequest;
import com.example.demo.dto.response.MessageResponse;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class MessageService {
    private final MessageRoomRepository messageRoomRepository;
    private final MessageContentRepository messageContentRepository;
    private final UserRepository userRepository;
    private final JobRepository jobRepository; // Nếu có bảng Job

    public MessageRoom getOrCreateRoomByJob(Long jobId, Long otherUserId) {
        String currentUserEmail = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new RuntimeException("Bạn chưa đăng nhập"));

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Job job = jobRepository.findById(jobId)
                .orElseThrow(() -> new RuntimeException("Job not found"));

        // ⚡️ Kiểm tra role
        String currentRoleName = (currentUser.getRole() != null)
                ? currentUser.getRole().getName()
                : null;

        User candidateUser;
        User employerUser;

        // 🧑‍💼 Nếu là HR → nhắn cho ứng viên
        if ("HR".equalsIgnoreCase(currentRoleName)) {
            employerUser = currentUser;
            if (otherUserId == null) {
                throw new RuntimeException("Không tìm thấy ứng viên để nhắn tin");
            }
            candidateUser = userRepository.findById(otherUserId)
                    .orElseThrow(() -> new RuntimeException("Ứng viên không tồn tại"));
        } else {
            // 🧍 Nếu là ứng viên (role == null hoặc khác HR)
            candidateUser = currentUser;
            employerUser = userRepository.findByCompanyIdAndRoleName(
                            job.getCompany().getId(), "HR")
                    .orElseThrow(() -> new RuntimeException(
                            "Công ty này chưa có HR. Vui lòng liên hệ admin"));
        }

        // 🔄 Kiểm tra phòng chat có sẵn chưa
        return messageRoomRepository
                .findByCandidateIdAndEmployerIdAndJobId(
                        candidateUser.getId(), employerUser.getId(), jobId)
                .orElseGet(() -> {
                    MessageRoom room = MessageRoom.builder()
                            .candidate(candidateUser)
                            .employer(employerUser)
                            .job(job)
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
    public List<MessageRoom> getMyRooms() {
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

        if (roleName == null || !"HR".equalsIgnoreCase(roleName)) {
            return messageRoomRepository.findByCandidateId(currentUser.getId());
        }

        return messageRoomRepository.findByEmployerId(currentUser.getId());
    }
}
