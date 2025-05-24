package com.videoplatform.repository;

import com.videoplatform.model.Like;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

    boolean existsByUserAndEntityIdAndEntityType(User user, Long entityId, String entityType);

    Optional<Like> findByUserAndEntityIdAndEntityType(User user, Long entityId, String entityType);

    long countByEntityIdAndEntityType(Long entityId, String entityType);
}