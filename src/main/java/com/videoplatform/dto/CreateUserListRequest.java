package com.videoplatform.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;

@Data
public class CreateUserListRequest {
    @NotBlank
    private String name;
    private String description;
}

