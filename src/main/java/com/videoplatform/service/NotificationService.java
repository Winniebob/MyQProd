package com.videoplatform.service;

import com.videoplatform.dto.NotificationDTO;
import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import com.videoplatform.repository.NotificationRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Создает уведомление и пушит его по WebSocket в /topic/notifications/{username}
     */
    @Transactional
    public NotificationDTO createNotification(
            String username,
            Notification.NotificationType type,    // <-- используем модельный enum
            String message
    ) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Notification n = Notification.builder()
                .user(user)
                .type(type)             // <-- поле уже есть в сущности
                .message(message)       // <-- поле message в вашей сущности
                .isRead(false)
                .build();

        n = notificationRepository.save(n);

        NotificationDTO dto = mapToDto(n);
        // пушим в websocket
        messagingTemplate.convertAndSend(
                "/topic/notifications/" + username,
                dto
        );
        return dto;
    }

    public List<NotificationDTO> getNotifications(
            Principal principal,
            Notification.NotificationType type
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return (type == null
                ? notificationRepository.findByUser(user)
                : notificationRepository.findByUserAndType(user, type))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(
            Principal principal,
            Notification.NotificationType type
    ) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        return (type == null
                ? notificationRepository.findByUserAndIsReadFalse(user)
                : notificationRepository.findByUserAndTypeAndIsReadFalse(user, type))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long notificationId, Principal principal) {
        notificationRepository.findById(notificationId).ifPresent(notification -> {
            if (notification.getUser().getUsername().equals(principal.getName())) {
                notification.setIsRead(true);
                notificationRepository.save(notification);
            }
        });
    }

    private NotificationDTO mapToDto(Notification n) {
        return NotificationDTO.builder()
                .id(n.getId())
                .type(n.getType())
                .message(n.getMessage())
                .isRead(n.getIsRead())
                .createdAt(n.getCreatedAt())
                .build();
    }
}