package com.videoplatform.repository;

import com.videoplatform.model.Message;
import com.videoplatform.model.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByConversationOrderBySentAtAsc(Conversation conversation);
}