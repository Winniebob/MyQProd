package com.videoplatform.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class SendMessageRequest {
    @NotBlank
    private String content;
}