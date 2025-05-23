package com.videoplatform.service;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.model.Comment;
import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import com.videoplatform.repository.CommentRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;

    // Добавить комментарий
    public CommentDTO addComment(CreateCommentRequest req, Principal principal) {
        User author = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        Video video = videoRepository.findById(req.getVideoId())
                .orElseThrow(() -> new NoSuchElementException("Video not found"));

        Comment parent = null;
        if (req.getParentId() != null) {
            parent = commentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new NoSuchElementException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .video(video)
                .author(author)
                .parent(parent)
                .text(req.getText())
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToDtoWithChildren(saved);
    }

    public Page<CommentDTO> listRootComments(Long videoId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt"));
        Page<Comment> pageResult = commentRepository.findByVideoIdAndParentIsNull(videoId, pageable);
        return pageResult.map(this::mapToDtoWithChildren);
    }

    public List<CommentDTO> listCommentTree(Long videoId) {
        List<Comment> roots = commentRepository.findByVideoIdAndParentIsNull(videoId, Sort.by(Sort.Direction.ASC, "createdAt"));
        return roots.stream()
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long id, Principal principal) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Comment not found"));
        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private CommentDTO mapToDtoWithChildren(Comment comment) {
        List<CommentDTO> children = commentRepository.findByParentId(comment.getId(), Sort.by("createdAt")).stream()
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList());

        return CommentDTO.builder()
                .id(comment.getId())
                .videoId(comment.getVideo().getId())
                .parentId(comment.getParent() != null ? comment.getParent().getId() : null)
                .authorUsername(comment.getAuthor().getUsername())
                .text(comment.getText())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .deleted(comment.getDeleted())
                .children(children)
                .build();
    }
}