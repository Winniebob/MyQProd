package com.videoplatform.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LikeResponse {
    private boolean liked;     // поставил ли текущий пользователь лайк
    private int totalLikes;    // текущее общее число лайков у видео
}