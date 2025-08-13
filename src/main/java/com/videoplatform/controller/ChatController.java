package com.videoplatform.controller;

import com.videoplatform.dto.ChatDTO;
import com.videoplatform.dto.ChatMessageDTO;
import com.videoplatform.dto.MessageDTO;
import com.videoplatform.dto.SendMessageRequest;
import com.videoplatform.model.ChatMessage;
import com.videoplatform.model.Stream;
import com.videoplatform.service.ChatService;
import com.videoplatform.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;
    private final StreamService streamService;

    @GetMapping
    public ResponseEntity<List<ChatDTO>> listConversations(Principal principal) {
        return ResponseEntity.ok(chatService.getConversations(principal));
    }

    @GetMapping("/{id}/messages")
    public ResponseEntity<List<MessageDTO>> getMessages(@PathVariable Long id) {
        return ResponseEntity.ok(chatService.getMessages(id));
    }

    @PostMapping("/{id}/messages")
    public ResponseEntity<MessageDTO> postMessage(
            @PathVariable Long id,
            @RequestBody SendMessageRequest req,
            Principal principal) {
        return ResponseEntity.ok(chatService.sendMessage(id, req, principal));
    }
    @MessageMapping("/chat/{streamKey}")
    public void onMessage(
            @DestinationVariable String streamKey,
            @Payload ChatMessageDTO incoming,
            Principal principal
    ) {
        // 1) Проверяем, что стрим есть и запущен
        Stream stream = streamService.getStreamByKey(streamKey);
        if (stream.getStatus() != Stream.StreamStatus.LIVE) {
            return; // игнорируем, если стрим не в LIVE
        }
        // 2) Сохраняем в БД
        ChatMessage saved = chatService.saveMessage(
                stream.getId(),
                principal.getName(),
                incoming.getContent()
        );
        // 3) Формируем DTO с id и sentAt
        ChatMessageDTO dto = new ChatMessageDTO();
        dto.setId(saved.getId());
        dto.setStreamId(saved.getStreamId());
        dto.setSender(saved.getSender());
        dto.setContent(saved.getContent());
        dto.setSentAt(saved.getSentAt());

        // 4) Рассылаем всем подписчикам на /topic/chat/{streamKey}
        messagingTemplate.convertAndSend(
                "/topic/chat/" + streamKey,
                dto
        );
    }
}