package com.videoplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull
    private Long videoId;

    // Optional: if replying to another comment
    private Long parentId;

    @NotBlank
    private String text;
}