package com.videoplatform.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class CreatePostRequest {
    @NotBlank(message = "Content cannot be blank")
    private String content;
}
