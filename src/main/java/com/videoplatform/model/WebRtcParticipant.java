package com.videoplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webrtc_participants",
        indexes = {
                @Index(name = "idx_webrtc_part_room", columnList = "room_id"),
                @Index(name = "idx_webrtc_part_user", columnList = "user_id")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WebRtcParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Комната, к которой относится участник */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private WebRtcRoom room;

    /** Пользователь (может быть null для анонимного зрителя) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Роль в комнате */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WebRtcRole role;

    /** Janus handle участника */
    @Column(name = "janus_handle_id")
    private String janusHandleId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    private LocalDateTime leftAt;
}