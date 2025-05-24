package com.videoplatform.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "streams")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stream {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Пользователь — владелец стрима
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(unique = true)
    private String streamKey;

    @Column(name = "is_live")
    private boolean isLive;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamStatus status;

    private LocalDateTime startedAt;

    private LocalDateTime stoppedAt;

    private String streamUrl;

    private String recordingUrl;

    public enum StreamStatus {
        CREATED,
        LIVE,
        STOPPED
    }
}
