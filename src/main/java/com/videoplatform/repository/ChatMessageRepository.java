package com.videoplatform.repository;

import com.videoplatform.model.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByStreamIdOrderBySentAtAsc(Long streamId);
}