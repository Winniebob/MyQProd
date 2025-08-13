package com.videoplatform.service;

import com.videoplatform.dto.ChatDTO;
import com.videoplatform.dto.MessageDTO;
import com.videoplatform.dto.SendMessageRequest;
import com.videoplatform.model.Conversation;
import com.videoplatform.model.Message;
import com.videoplatform.model.User;
import com.videoplatform.repository.ConversationRepository;
import com.videoplatform.repository.MessageRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository convRepo;
    private final MessageRepository msgRepo;
    private final UserRepository userRepo;

    public List<ChatDTO> getConversations(Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        List<Conversation> convs = convRepo.findByParticipantsContains(user);
        return convs.stream().map(conv -> {
            List<Message> msgs = msgRepo.findByConversationOrderBySentAtAsc(conv);
            Message last = msgs.isEmpty() ? null : msgs.get(msgs.size() - 1);
            Set<String> participants = conv.getParticipants().stream()
                    .map(User::getUsername).collect(Collectors.toSet());
            return ChatDTO.builder()
                    .conversationId(conv.getId())
                    .participantUsernames(participants)
                    .lastMessage(last != null ? last.getContent() : null)
                    .lastTimestamp(last != null ? last.getSentAt() : null)
                    .build();
        }).collect(Collectors.toList());
    }

    public List<MessageDTO> getMessages(Long convId) {
        Conversation conv = convRepo.findById(convId).orElseThrow();
        return msgRepo.findByConversationOrderBySentAtAsc(conv).stream()
                .map(msg -> MessageDTO.builder()
                        .id(msg.getId())
                        .conversationId(convId)
                        .senderUsername(msg.getSender().getUsername())
                        .content(msg.getContent())
                        .sentAt(msg.getSentAt())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public MessageDTO sendMessage(Long convId, SendMessageRequest req, Principal principal) {
        Conversation conv = convRepo.findById(convId).orElseThrow();
        User sender = userRepo.findByUsername(principal.getName()).orElseThrow();
        Message msg = Message.builder()
                .conversation(conv)
                .sender(sender)
                .content(req.getContent())
                .sentAt(LocalDateTime.now())
                .build();
        msg = msgRepo.save(msg);
        return MessageDTO.builder()
                .id(msg.getId())
                .conversationId(convId)
                .senderUsername(sender.getUsername())
                .content(msg.getContent())
                .sentAt(msg.getSentAt())
                .build();
    }
}