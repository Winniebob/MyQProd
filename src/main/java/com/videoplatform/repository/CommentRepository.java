package com.videoplatform.repository;

import com.videoplatform.model.Comment;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findTop5ByAuthorOrderByCreatedAtDesc(User author);
    List<Comment> findByVideoId(Long videoId);

    List<Comment> findByParentId(Long parentId);

    List<Comment> findByVideoIdAndDeletedFalse(Long videoId);

    Optional<Comment> findByIdAndDeletedFalse(Long id);
    // Метод для получения последнего комментария пользователя
    Comment findTopByAuthorOrderByCreatedAtDesc(User author);
}