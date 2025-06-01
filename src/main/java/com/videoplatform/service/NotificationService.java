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

    /**
     * Рассылает уведомление списку пользователей.
     *
     * @param recipients список получателей
     * @param type       тип уведомления
     * @param message    текст уведомления
     * @param streamId   id стрима (может быть null)
     */
    public void notifyUsers(List<User> recipients,
                            Notification.NotificationType type,
                            String message,
                            Long streamId) {
        for (User recipient : recipients) {
            Boolean enabled = recipient.getNotificationSettings() != null
                    ? recipient.getNotificationSettings().getOrDefault(type.name(), true)
                    : true;
            if (!enabled) continue;

            Notification notification = new Notification();
            notification.setRecipient(recipient);
            notification.setType(type);
            notification.setMessage(message);
            notification.setRead(false);
            notification.setCreatedAt(LocalDateTime.now());
            notification.setStreamId(streamId);
            notificationRepository.save(notification);
        }
    }

    /**
     * Получить все уведомления (по типу, если type != null) для текущего пользователя.
     */
    public List<NotificationDTO> getNotifications(Principal principal, Notification.NotificationType type) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;
        if (type != null) {
            notifications = notificationRepository
                    .findByRecipientAndTypeOrderByCreatedAtDesc(user, type);
        } else {
            notifications = notificationRepository
                    .findByRecipientOrderByCreatedAtDesc(user);
        }

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Получить непрочитанные уведомления (по типу, если type != null) для текущего пользователя.
     */
    public List<NotificationDTO> getUnreadNotifications(Principal principal, Notification.NotificationType type) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Notification> notifications;
        if (type != null) {
            notifications = notificationRepository
                    .findByRecipientAndReadFalseAndType(user, type);
        } else {
            notifications = notificationRepository
                    .findByRecipientAndReadFalse(user);
        }

        return notifications.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Пометить уведомление как прочитанное.
     */
    public void markAsRead(Long id, Principal principal) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        if (!notification.getRecipient().getUsername().equals(principal.getName())) {
            throw new RuntimeException("No permission to mark this notification");
        }

        notification.setRead(true);
        notificationRepository.save(notification);
    }

    /**
     * Получить настройки уведомлений для пользователя.
     */
    public Map<String, Boolean> getUserNotificationSettings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.getNotificationSettings();
    }

    /**
     * Обновить настройки уведомлений пользователя.
     */
    public void updateUserNotificationSettings(Long userId, Map<String, Boolean> settings) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setNotificationSettings(settings);
        userRepository.save(user);
    }

    /**
     * Старая сигнатура (без передачи streamId). Просто делегируем null.
     */
    public void createNotification(User recipient,
                                   Notification.NotificationType type,
                                   String message) {
        createNotification(recipient, type, message, null);
    }

    /**
     * Основной метод: создаёт уведомление и сохраняет поле streamId.
     */
    public void createNotification(User recipient,
                                   Notification.NotificationType type,
                                   String message,
                                   Long streamId) {
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
        notification.setStreamId(streamId);
        notificationRepository.save(notification);
    }

    private NotificationDTO mapToDto(Notification notification) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(notification.getId());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setStreamId(notification.getStreamId());
        return dto;
    }
}