package com.videoplatform.repository;

import com.videoplatform.model.Video;
import com.videoplatform.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    List<Video> findByUser(User user);
    List<Video> findByTitleContainingIgnoreCase(String title);
    List<Video> findTop10ByIsPublicTrueOrderByCreatedAtDesc();
    List<Video> findTop10ByIsRecommendedTrueOrderByViewsDesc();
    List<Video> findByUserAndIsPublicTrue(User user);

    Optional<Video> findByVideoUrl(String videoUrl);
    // Additional pagination for list
    Page<Video> findAllByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
}