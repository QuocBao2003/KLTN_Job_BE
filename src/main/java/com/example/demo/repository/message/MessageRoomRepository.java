package com.example.demo.repository.message;


import com.example.demo.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRoomRepository  extends JpaRepository<MessageRoom, UUID> {
    Optional<MessageRoom> findByCandidateIdAndEmployerIdAndJobId(Long candidateId, Long employerId, Long jobId);

    // ✅ Sắp xếp theo thời gian tin nhắn cuối cùng
    List<MessageRoom> findByEmployerIdOrderByLastMessageTimeDesc(Long employerId);
    List<MessageRoom> findByCandidateIdOrderByLastMessageTimeDesc(Long candidateId);

    // ✅ Đếm số PHÒNG có tin nhắn chưa đọc (số người gửi)
    @Query("SELECT COUNT(r) FROM MessageRoom r WHERE r.candidate.id = :candidateId AND r.candidateUnreadCount > 0")
    Integer countRoomsWithUnreadForCandidate(@Param("candidateId") Long candidateId);

    @Query("SELECT COUNT(r) FROM MessageRoom r WHERE r.employer.id = :employerId AND r.employerUnreadCount > 0")
    Integer countRoomsWithUnreadForEmployer(@Param("employerId") Long employerId);

    // ✅ Reset tất cả unread count khi click vào icon Message
    @Modifying
    @Query("UPDATE MessageRoom r SET r.candidateUnreadCount = 0 WHERE r.candidate.id = :candidateId")
    void resetCandidateUnreadCount(@Param("candidateId") Long candidateId);

    @Modifying
    @Query("UPDATE MessageRoom r SET r.employerUnreadCount = 0 WHERE r.employer.id = :employerId")
    void resetEmployerUnreadCount(@Param("employerId") Long employerId);
}

