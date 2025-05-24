package com.videoplatform.service;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.model.Comment;
import com.videoplatform.model.User;
import com.videoplatform.repository.CommentRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;
import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CommentService {

    private static final long COMMENT_COOLDOWN_SECONDS = 30;

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentDTO addComment(CreateCommentRequest req, Principal principal) {
        final int MAX_COMMENT_LENGTH = 1000;
        final long COMMENT_COOLDOWN_SECONDS = 30;

        String commentText = req.getText();
        if (commentText == null || commentText.trim().isEmpty()) {
            throw new IllegalArgumentException("Комментарий не может быть пустым");
        }
        if (commentText.length() > MAX_COMMENT_LENGTH) {
            throw new IllegalArgumentException("Комментарий слишком длинный. Максимум " + MAX_COMMENT_LENGTH + " символов.");
        }

        User author = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        // Проверяем время последнего комментария пользователя
        Comment lastComment = commentRepository.findTopByAuthorOrderByCreatedAtDesc(author);
        if (lastComment != null) {
            Duration duration = Duration.between(lastComment.getCreatedAt(), LocalDateTime.now());
            if (duration.getSeconds() < COMMENT_COOLDOWN_SECONDS) {
                throw new RuntimeException("Вы комментируете слишком часто. Подождите немного.");
            }
        }

        // Проверка на повторяющийся текст с последних 5 комментариев
        List<Comment> recentComments = commentRepository.findTop5ByAuthorOrderByCreatedAtDesc(author);
        for (Comment c : recentComments) {
            if (c.getText().equalsIgnoreCase(commentText.trim())) {
                throw new IllegalArgumentException("Похожий комментарий уже был добавлен недавно");
            }
        }

        var video = videoRepository.findById(req.getVideoId())
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
                .text(commentText)
                .createdAt(LocalDateTime.now())
                .deleted(false)
                .build();

        Comment saved = commentRepository.save(comment);

        if (parent != null && !parent.getAuthor().equals(author)) {
            notificationService.createNotification(parent.getAuthor(), "Ваш комментарий получил ответ.");
        }

        return mapToDtoWithChildren(saved);
    }

    @Cacheable(value = "commentsTree", key = "#videoId")
    public List<CommentDTO> getCommentsTreeByVideoId(Long videoId) {
        List<Comment> comments = commentRepository.findByVideoIdAndDeletedFalse(videoId);

        Map<Long, CommentDTO> dtoMap = new HashMap<>();
        List<CommentDTO> roots = new ArrayList<>();

        for (Comment c : comments) {
            CommentDTO dto = mapToDto(c);
            dto.setChildren(new ArrayList<>());
            dtoMap.put(c.getId(), dto);
        }

        for (Comment c : comments) {
            CommentDTO dto = dtoMap.get(c.getId());
            if (c.getParent() != null) {
                CommentDTO parentDto = dtoMap.get(c.getParent().getId());
                if (parentDto != null) {
                    parentDto.getChildren().add(dto);
                }
            } else {
                roots.add(dto);
            }
        }

        return roots;
    }

    @Transactional
    public void softDeleteComment(Long commentId, String username) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NoSuchElementException("Комментарий не найден"));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("Недостаточно прав для удаления комментария");
        }

        comment.setDeleted(true);
        commentRepository.save(comment);
    }

    private CommentDTO mapToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setVideoId(comment.getVideo().getId());
        dto.setParentId(comment.getParent() != null ? comment.getParent().getId() : null);
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setText(comment.getText());
        dto.setCreatedAt(comment.getCreatedAt());
        dto.setUpdatedAt(comment.getUpdatedAt());
        dto.setDeleted(comment.getDeleted());
        return dto;
    }

    private CommentDTO mapToDtoWithChildren(Comment comment) {
        CommentDTO dto = mapToDto(comment);
        List<CommentDTO> children = new ArrayList<>();

        List<Comment> childComments = commentRepository.findByParentId(comment.getId());
        for (Comment child : childComments) {
            if (Boolean.TRUE.equals(child.getDeleted())) continue;
            children.add(mapToDtoWithChildren(child));
        }
        dto.setChildren(children);
        return dto;
    }
    @Transactional
    public CommentDTO updateComment(Long commentId, String newText, String username) {
        Comment comment = commentRepository.findByIdAndDeletedFalse(commentId)
                .orElseThrow(() -> new NoSuchElementException("Комментарий не найден"));

        if (!comment.getAuthor().getUsername().equals(username)) {
            throw new SecurityException("Недостаточно прав для редактирования комментария");
        }

        comment.setText(newText);
        comment.setUpdatedAt(LocalDateTime.now());
        Comment updated = commentRepository.save(comment);

        return mapToDto(updated);
    }
}