package com.videoplatform.controller;

import com.videoplatform.dto.NotificationDTO;
import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserRepository userRepository;  // <-- добавь это

    @GetMapping
    public ResponseEntity<List<NotificationDTO>> getNotifications(
            Principal principal,
            @RequestParam(value = "type", required = false) Notification.NotificationType type) {

        return ResponseEntity.ok(notificationService.getNotifications(principal, type));
    }

    @GetMapping("/unread")
    public ResponseEntity<List<NotificationDTO>> getUnread(
            Principal principal,
            @RequestParam(value = "type", required = false) Notification.NotificationType type) {

        return ResponseEntity.ok(notificationService.getUnreadNotifications(principal, type));
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, Principal principal) {
        notificationService.markAsRead(id, principal);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/settings")
    public ResponseEntity<Map<String, Boolean>> getSettings(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        Map<String, Boolean> settings = notificationService.getUserNotificationSettings(user.getId());
        return ResponseEntity.ok(settings);
    }

    @PutMapping("/settings")
    public ResponseEntity<?> updateSettings(@RequestBody Map<String, Boolean> settings, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        notificationService.updateUserNotificationSettings(user.getId(), settings);
        return ResponseEntity.ok().build();
    }
}
