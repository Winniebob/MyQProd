package com.videoplatform.service;

import com.videoplatform.dto.LikeResponse;
import com.videoplatform.model.Like;
import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import com.videoplatform.repository.LikeRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    /**
     * Переключает лайк:
     * - если уже лайк было — удаляет,
     * - иначе — создаёт.
     * Возвращает DTO с флагом (liked) и актуальным количеством лайков.
     */
    @Transactional
    public LikeResponse toggleLike(Long videoId, Principal principal) {
        // 1) Получаем текущего пользователя
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2) Ищем видео по ID
        Video video = videoRepository.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // 3) Проверяем, поставлен ли уже лайк
        boolean nowLiked;
        if (likeRepository.findByUserAndVideo(user, video).isPresent()) {
            // если есть — удаляем
            likeRepository.deleteByUserAndVideo(user, video);
            nowLiked = false;
        } else {
            // если нет — создаём новый лайк
            Like like = Like.builder()
                    .user(user)
                    .video(video)
                    .build();
            likeRepository.save(like);
            nowLiked = true;
        }

        // 4) Считаем общее число лайков у видео
        int totalLikes = likeRepository.countByVideo(video);

        return new LikeResponse(nowLiked, totalLikes);
    }
}