package com.videoplatform.repository;

import com.videoplatform.model.Comment;

import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.data.domain.Page;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByVideoIdAndParentIsNull(Long videoId, Pageable pageable);
    List<Comment> findByVideoIdAndParentIsNull(Long videoId, Sort sort);
    List<Comment> findByParentId(Long parentId, Sort sort);
}