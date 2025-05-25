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

    @ManyToMany
    @JoinTable(
            name = "stream_participants",
            joinColumns = @JoinColumn(name = "stream_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private java.util.List<User> participants;

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
    @Column(name = "group_name")
    private String groupName;
    @Column(name = "is_live")
    private Boolean isLive;
    private boolean isPublic;

    private LocalDateTime startedAt;

    private LocalDateTime stoppedAt;

    private String streamUrl;

    private String recordingUrl;
//
    public enum StreamStatus {
        CREATED, LIVE, STOPPED
    }
}