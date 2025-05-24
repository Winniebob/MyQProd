package com.videoplatform.service;

import com.videoplatform.dto.NotificationDTO;
import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import com.videoplatform.repository.NotificationRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    public void createNotification(User recipient, String message) {
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }

    public List<NotificationDTO> getNotifications(Principal principal, Notification.NotificationType type) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;

        if (type != null) {
            notifications = notificationRepository.findByRecipientAndTypeOrderByCreatedAtDesc(user, type);
        } else {
            notifications = notificationRepository.findByRecipientOrderByCreatedAtDesc(user);
        }

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<NotificationDTO> getUnreadNotifications(Principal principal, Notification.NotificationType type) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;

        if (type != null) {
            notifications = notificationRepository.findByRecipientAndReadFalseAndType(user, type);
        } else {
            notifications = notificationRepository.findByRecipientAndReadFalse(user);
        }

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public void markAsRead(Long id, Principal principal) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getUsername().equals(principal.getName())) {
            throw new RuntimeException("No permission to mark this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    private NotificationDTO mapToDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        return dto;
    }
    public Map<String, Boolean> getUserNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getNotificationSettings();
    }

    public void updateUserNotificationSettings(Long userId, Map<String, Boolean> settings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNotificationSettings(settings);
        userRepository.save(user);
    }
    public void createNotification(User recipient, Notification.NotificationType type, String message) {
        Boolean enabled = recipient.getNotificationSettings() != null
                ? recipient.getNotificationSettings().getOrDefault(type.name(), true)
                : true;
        if (!enabled) {
            return;
        }
        Notification notification = new Notification();
        notification.setRecipient(recipient);
        notification.setType(type);
        notification.setMessage(message);
        notification.setRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        notificationRepository.save(notification);
    }
}