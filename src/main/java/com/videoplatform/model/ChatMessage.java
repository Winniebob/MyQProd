package com.videoplatform.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages")
public class ChatMessage {
    @Id
    @GeneratedValue Long id;
    @Column(name="stream_id", nullable=false) Long streamId;
    @Column(nullable=false) String sender;      // username или id
    @Column(nullable=false) String content;
    @Column(name="sent_at", nullable=false)
    LocalDateTime sentAt;
}