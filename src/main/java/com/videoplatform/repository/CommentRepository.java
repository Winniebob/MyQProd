package com.videoplatform.repository;

import com.videoplatform.model.Comment;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // Только «корневые» комментарии под видео
    List<Comment> findByVideoIdAndParentIsNull(Long videoId, Sort sort);

    // Все непосредственные дети данного комментария
    List<Comment> findByParentId(Long parentId, Sort sort);
}