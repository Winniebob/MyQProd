package com.videoplatform.repository;

import com.videoplatform.model.Like;
import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {
    Optional<Like> findByUserAndVideo(User user, Video video);
    void deleteByUserAndVideo(User user, Video video);
    int countByVideo(Video video);
}