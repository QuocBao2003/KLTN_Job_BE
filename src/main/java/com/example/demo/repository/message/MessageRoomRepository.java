package com.example.demo.repository.message;


import com.example.demo.domain.MessageRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MessageRoomRepository  extends JpaRepository<MessageRoom, UUID> {
    Optional<MessageRoom> findByCandidateIdAndEmployerIdAndJobId(Long candidateId, Long employerId, Long jobId);

    List<MessageRoom> findByEmployerId(Long employerId);

    List<MessageRoom> findByCandidateId(Long candidateId);
}
