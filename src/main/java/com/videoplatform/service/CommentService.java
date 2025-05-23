package com.videoplatform.service;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.dto.UpdateCommentRequest;
import com.videoplatform.exception.NotFoundException;
import com.videoplatform.model.Comment;
import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import com.videoplatform.repository.CommentRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {
    private final CommentRepository commentRepository;
    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    /** 1. Добавление комментария (включая reply) */
    @Transactional
    public CommentDTO addComment(CreateCommentRequest req, Principal principal) {
        Video video = videoRepository.findById(req.getVideoId())
                .orElseThrow(() -> new NotFoundException("Video not found"));
        User author = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));

        Comment parent = null;
        if (req.getParentId() != null) {
            parent = commentRepository.findById(req.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        Comment comment = Comment.builder()
                .video(video)
                .author(author)
                .parent(parent)
                .text(req.getText())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);
        return mapToDtoWithChildren(saved);
    }

    /** 2. Обновление текста комментария */
    @Transactional
    public CommentDTO updateComment(Long id, UpdateCommentRequest req, Principal principal) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        // тут можно проверить, что principal соответствует author…

        comment.setText(req.getText());
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updated = commentRepository.save(comment);
        return mapToDtoWithChildren(updated);
    }

    /** 3. Удаление (можно мягкое или реальное) */
    @Transactional
    public void deleteComment(Long id, Principal principal) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        // проверка principal == comment.getAuthor() ...
        commentRepository.delete(comment);
    }

    /** 4. Пагинация корневых комментариев */
    public List<CommentDTO> listRootComments(Long videoId, int page, int size) {
        Sort sort = Sort.by("createdAt").descending();
        return commentRepository.findByVideoIdAndParentIsNull(videoId, sort).stream()
                .skip((long) page * size)
                .limit(size)
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList());
    }

    /** 5. Полное дерево комментариев без пагинации */
    public List<CommentDTO> listCommentTree(Long videoId) {
        Sort sort = Sort.by("createdAt");
        List<Comment> roots = commentRepository.findByVideoIdAndParentIsNull(videoId, sort);
        return roots.stream()
                .map(this::mapToDtoWithChildren)
                .collect(Collectors.toList());
    }

    /** Вспомогательный метод — строим рекурсивно дерево в DTO */
    private CommentDTO mapToDtoWithChildren(Comment comment) {
        List<CommentDTO> children = commentRepository.findByParentId(comment.getId(), Sort.by("createdAt"))
                .stream()
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
                .children(children)
                .build();
    }
}