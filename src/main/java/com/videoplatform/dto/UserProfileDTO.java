package com.videoplatform.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class UserProfileDTO {
    private Long id;
    private String username;
    private String displayName;
    private String bio;
    private String avatarUrl;
    private LocalDateTime createdAt;
    private List<VideoDTO> videos;
    private long  subscribersCount;
    private boolean isSubscribed; // если текущий пользователь подписан
    private String coverUrl;
}