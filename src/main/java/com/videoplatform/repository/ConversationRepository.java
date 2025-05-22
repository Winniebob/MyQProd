package com.videoplatform.repository;

import com.videoplatform.model.Conversation;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findByParticipantsContains(User user);
}