package com.videoplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommentDTO {
    private Long id;
    private Long userId;
    private String username;
    private String text;
    private LocalDateTime createdAt;
    private Long parentId;              // <- теперь есть!
    private List<CommentDTO> replies;   // <- для дерева
}