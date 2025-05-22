package com.videoplatform.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatDTO {
    private Long conversationId;
    private Set<String> participantUsernames;
    private String lastMessage;
    private LocalDateTime lastTimestamp;
}