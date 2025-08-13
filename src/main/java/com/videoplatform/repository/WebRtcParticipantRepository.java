package com.videoplatform.repository;

import com.videoplatform.model.WebRtcParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WebRtcParticipantRepository extends JpaRepository<WebRtcParticipant, Long> {

    List<WebRtcParticipant> findByRoom_Id(Long roomId);

    long countByRoom_IdAndLeftAtIsNull(Long roomId);

    Optional<WebRtcParticipant> findByRoom_IdAndUser_IdAndLeftAtIsNull(Long roomId, Long userId);
}