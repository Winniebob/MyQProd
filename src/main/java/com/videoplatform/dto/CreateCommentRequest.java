package com.videoplatform.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateCommentRequest {
    @NotNull
    private Long videoId;

    @NotBlank(message = "Text не может быть пустым")
    private String text;

    // Если это ответ на комментарий — его ID, иначе null
    private Long parentId;
}