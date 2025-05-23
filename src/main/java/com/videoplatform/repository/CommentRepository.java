package com.videoplatform.repository;

import com.videoplatform.model.Comment;
import com.videoplatform.model.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

    // постраничная выдача только корневых
    Page<Comment> findByVideoAndParentIsNull(Video video, Pageable pageable);

    // для полного дерева (List + Sort)
    List<Comment> findByVideoAndParentIsNull(Video video, Sort sort);

    List<Comment> findByParentId(Long parentId, Sort sort);

    // для полного дерева ответов
    List<Comment> findByParent(Comment parent, Sort sort);
}