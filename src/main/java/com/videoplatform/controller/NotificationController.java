package com.videoplatform.controller;

import com.videoplatform.dto.NotificationDTO;
import com.videoplatform.model.Notification.NotificationType;
import com.videoplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            Principal principal,
            @RequestParam(value = "type", required = false) NotificationType type
    ) {
        return ResponseEntity.ok(notificationService.getNotifications(principal, type));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(
            Principal principal,
            @RequestParam(value = "type", required = false) NotificationType type
    ) {
        return ResponseEntity.ok(notificationService.getUnreadNotifications(principal, type));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id,
                                        Principal principal) {
        notificationService.markAsRead(id, principal);
        return ResponseEntity.ok().build();
    }
}
