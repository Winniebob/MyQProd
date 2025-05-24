package com.videoplatform.repository;

import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    List<Notification> findByRecipientAndReadFalse(User recipient);

    List<Notification> findByRecipientAndTypeOrderByCreatedAtDesc(User recipient, Notification.NotificationType type);

    List<Notification> findByRecipientAndReadFalseAndType(User recipient, Notification.NotificationType type);
}
