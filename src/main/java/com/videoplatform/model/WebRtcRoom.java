package com.videoplatform.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "webrtc_rooms",
        indexes = {
                @Index(name = "idx_webrtc_room_stream", columnList = "stream_id"),
                @Index(name = "idx_webrtc_room_status", columnList = "status")
        })
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class WebRtcRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** К какому стриму относится комната */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "stream_id", nullable = false)
    private Stream stream;

    /** Владелец/создатель комнаты */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    /** Идентификаторы Janus */
    @Column(name = "janus_session_id")
    private String janusSessionId;

    @Column(name = "janus_handle_id")
    private String janusHandleId;

    @Column(name = "janus_room_id")
    private String janusRoomId;

    @Column(name = "pin")
    private String pin;

    @Column(name = "secret")
    private String secret;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private WebRtcRoomStatus status = WebRtcRoomStatus.ACTIVE;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}