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

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, unique = true)
    private String streamKey;

    private String webrtcSessionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StreamStatus status;

    @Column(name = "is_live")
    private Boolean isLive;

    private LocalDateTime startedAt;

    private LocalDateTime stoppedAt;

    private String streamUrl;

    private String recordingUrl;

    public enum StreamStatus {
        CREATED, LIVE, STOPPED
    }
}