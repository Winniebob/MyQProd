package com.videoplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentDTO {
    private Long id;
    private Long videoId;
    private Long parentId;
    private String authorUsername;
    private String text;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean deleted;
    private List<CommentDTO> children;
}