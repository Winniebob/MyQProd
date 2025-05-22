package com.videoplatform.controller;
import com.videoplatform.dto.*;
import com.videoplatform.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

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
}