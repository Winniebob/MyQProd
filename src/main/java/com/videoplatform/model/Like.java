package com.videoplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Like {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Кто поставил лайк
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // ID объекта (видео, комментария и т.п.)
    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    // Тип объекта, например "VIDEO", "COMMENT"
    @Column(name = "entity_type", nullable = false)
    private String entityType;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}