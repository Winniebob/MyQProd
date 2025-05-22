package com.videoplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Username пользователя — получателя уведомления */
    @Column(nullable = false)
    private String recipientUsername;

    /** Тип уведомления */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    /** Текст уведомления */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    /** Время создания */
    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Id

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    public enum NotificationType {
        NEW_COMMENT,
        NEW_LIKE,
        NEW_SUBSCRIPTION
    }
}
