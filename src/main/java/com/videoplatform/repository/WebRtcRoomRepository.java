package com.videoplatform.repository;

import com.videoplatform.model.WebRtcRoom;
import com.videoplatform.model.WebRtcRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WebRtcRoomRepository extends JpaRepository<WebRtcRoom, Long> {

    Optional<WebRtcRoom> findTopByStream_IdOrderByCreatedAtDesc(Long streamId);

    Optional<WebRtcRoom> findTopByStream_IdAndStatusOrderByCreatedAtDesc(Long streamId, WebRtcRoomStatus status);

    boolean existsByStream_IdAndStatus(Long streamId, WebRtcRoomStatus status);
}