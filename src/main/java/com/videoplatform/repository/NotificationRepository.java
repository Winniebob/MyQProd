package com.videoplatform.repository;

import com.videoplatform.model.Notification;
import com.videoplatform.model.Notification.NotificationType;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserAndIsReadFalse(User user);
    List<Notification> findByUserAndTypeAndIsReadFalse(User user, NotificationType type);
    List<Notification> findByUser(User user);
    List<Notification> findByUserAndType(User user, NotificationType type);
}