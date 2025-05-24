package com.videoplatform.service;

import com.videoplatform.dto.LikeDTO;
import com.videoplatform.model.Like;
import com.videoplatform.model.User;
import com.videoplatform.repository.LikeRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final UserRepository userRepository;

    public boolean addLike(Long entityId, String entityType, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        boolean alreadyLiked = likeRepository.existsByUserAndEntityIdAndEntityType(user, entityId, entityType);
        if (alreadyLiked) return false;

        Like like = Like.builder()
                .user(user)
                .entityId(entityId)
                .entityType(entityType)
                .createdAt(LocalDateTime.now())
                .build();

        likeRepository.save(like);
        return true;
    }

    public boolean removeLike(Long entityId, String entityType, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Optional<Like> likeOpt = likeRepository.findByUserAndEntityIdAndEntityType(user, entityId, entityType);
        if (likeOpt.isPresent()) {
            likeRepository.delete(likeOpt.get());
            return true;
        }
        return false;
    }

    public long countLikes(Long entityId, String entityType) {
        return likeRepository.countByEntityIdAndEntityType(entityId, entityType);
    }

    public boolean hasUserLiked(Long entityId, String entityType, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return likeRepository.existsByUserAndEntityIdAndEntityType(user, entityId, entityType);
    }
}