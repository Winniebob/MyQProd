package com.videoplatform.repository;

import com.videoplatform.model.Bookmark;
import com.videoplatform.model.User;
import com.videoplatform.model.Bookmark.TargetType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BookmarkRepository extends JpaRepository<Bookmark, Long> {
    List<Bookmark> findByUserAndTargetType(User user, TargetType type);
    Optional<Bookmark> findByUserAndTargetTypeAndTargetId(User user, TargetType type, Long targetId);

}