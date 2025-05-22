package com.videoplatform.service;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.dto.UpdateCommentRequest;
import com.videoplatform.exception.ForbiddenException;
import com.videoplatform.exception.NotFoundException;
import com.videoplatform.model.Comment;
import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import com.videoplatform.repository.CommentRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;
    private final VideoRepository    videoRepo;
    private final UserRepository     userRepo;
    private final NotificationService notificationSvc;

    @Transactional
    public CommentDTO addComment(CreateCommentRequest req, Principal principal) {
        User user = userRepo.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Video video = videoRepo.findById(req.getVideoId())
                .orElseThrow(() -> new NotFoundException("Video not found"));

        Comment parent = null;
        if (req.getParentId() != null) {
            parent = commentRepo.findById(req.getParentId())
                    .orElseThrow(() -> new NotFoundException("Parent comment not found"));
        }

        Comment c = Comment.builder()
                .user(user)
                .video(video)
                .text(req.getText())
                .parent(parent)
                .build();
        commentRepo.save(c);

        // уведомление автору видео (если это не он сам)
        if (!video.getUser().getId().equals(user.getId())) {
            notificationSvc.createNotification(
                    video.getUser().getUsername(),
                    Notification.NotificationType.NEW_COMMENT,
                    "Новое сообщение под вашим видео"
            );
        }

        return toDto(c);
    }

    @Transactional
    public CommentDTO updateComment(Long id, UpdateCommentRequest req, Principal principal) {
        Comment c = commentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        if (!c.getUser().getUsername().equals(principal.getName())) {
            throw new ForbiddenException("Access denied");
        }
        c.setText(req.getText());
        commentRepo.save(c);
        return toDto(c);
    }

    @Transactional
    public void deleteComment(Long id, Principal principal) {
        Comment c = commentRepo.findById(id)
                .orElseThrow(() -> new NotFoundException("Comment not found"));
        if (!c.getUser().getUsername().equals(principal.getName())) {
            // теперь передаём сообщение в конструктор
            throw new ForbiddenException("You are not allowed to delete this comment");
        }
        commentRepo.delete(c);
    }

    /** Пагинация только по корневым комментариям */
    public Page<CommentDTO> listRootComments(Long videoId, int page, int size) {
        Video video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));
        Pageable pp = PageRequest.of(page, size, Sort.by("createdAt").ascending());
        return commentRepo.findByVideoAndParentIsNull(video, pp)
                .map(this::toDto);
    }

    /** Полное дерево комментариев */
    public List<CommentDTO> listCommentTree(Long videoId) {
        Video video = videoRepo.findById(videoId)
                .orElseThrow(() -> new NotFoundException("Video not found"));

        List<Comment> roots = commentRepo.findByVideoAndParentIsNull(video, Sort.by("createdAt").ascending());
        return roots.stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
    }

    private CommentDTO toDto(Comment c) {
        return CommentDTO.builder()
                .id(c.getId())
                .userId(c.getUser().getId())
                .username(c.getUser().getUsername())
                .text(c.getText())
                .createdAt(c.getCreatedAt())
                .parentId(c.getParent() != null ? c.getParent().getId() : null)
                .build();
    }

    private CommentDTO toTreeDto(Comment c) {
        CommentDTO dto = toDto(c);
        List<CommentDTO> replies = commentRepo.findByParent(c, Sort.by("createdAt").ascending())
                .stream()
                .map(this::toTreeDto)
                .collect(Collectors.toList());
        dto.setReplies(replies);
        return dto;
    }

}