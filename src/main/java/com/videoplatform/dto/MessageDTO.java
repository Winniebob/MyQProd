package com.videoplatform.dto;

import java.time.LocalDateTime;

import lombok.*;
import java.time.LocalDateTime;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageDTO {
    private Long id;
    private Long conversationId;
    private String senderUsername;
    private String content;
    private LocalDateTime sentAt;



    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public String getSenderUsername() {
        return senderUsername;
    }

    public void setSenderUsername(String senderUsername) {
        this.senderUsername = senderUsername;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }

    public static MessageDTOBuilder builder() {
        return new MessageDTOBuilder();
    }

    public static class MessageDTOBuilder {
        private Long id;
        private Long conversationId;
        private String senderUsername;
        private String content;
        private LocalDateTime sentAt;

        MessageDTOBuilder() {}

        public MessageDTOBuilder id(Long id) {
            this.id = id;
            return this;
        }

        public MessageDTOBuilder conversationId(Long conversationId) {
            this.conversationId = conversationId;
            return this;
        }

        public MessageDTOBuilder senderUsername(String senderUsername) {
            this.senderUsername = senderUsername;
            return this;
        }

        public MessageDTOBuilder content(String content) {
            this.content = content;
            return this;
        }

        public MessageDTOBuilder sentAt(LocalDateTime sentAt) {
            this.sentAt = sentAt;
            return this;
        }

        public MessageDTO build() {
            MessageDTO dto = new MessageDTO();
            dto.setId(this.id);
            dto.setConversationId(this.conversationId);
            dto.setSenderUsername(this.senderUsername);
            dto.setContent(this.content);
            dto.setSentAt(this.sentAt);
            return dto;
        }
    }
}