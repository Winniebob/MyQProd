package com.videoplatform.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StreamDTO {
    private Long id;
    private String title;
    private String streamKey;
    private Boolean isLive;
    private LocalDateTime startedAt;
    private LocalDateTime endedAt;
    private String authorUsername;
}