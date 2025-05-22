package com.videoplatform.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostDTO {
    private Long id;
    private String authorUsername;
    private String content;
    private String mediaUrl;
    private LocalDateTime createdAt;
}