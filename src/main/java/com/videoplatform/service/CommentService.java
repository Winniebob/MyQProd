package com.videoplatform.service;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.exception.NotFoundException;
import com.videoplatform.model.Comment;
import com.videoplatform.model.User;
import com.videoplatform.repository.CommentRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final VideoRepository videoRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository,
                          UserRepository userRepository,
                          VideoRepository videoRepository,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
        this.videoRepository = videoRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public CommentDTO addComment(CreateCommentRequest req, Principal principal) {
        User author = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
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
                .text(req.getText())
                .createdAt(LocalDateTime.now())
                .build();

        Comment saved = commentRepository.save(comment);

        if (parent != null && !parent.getAuthor().equals(author)) {
            notificationService.createNotification(parent.getAuthor(), "Ваш комментарий получил ответ.");
        }

        return mapToDtoWithChildren(saved);
    }

    public List<CommentDTO> getCommentsTreeByVideoId(Long videoId) {
        List<Comment> comments = commentRepository.findByVideoId(videoId);

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

    private CommentDTO mapToDto(Comment comment) {
        CommentDTO dto = new CommentDTO();
        dto.setId(comment.getId());
        dto.setText(comment.getText());
        dto.setAuthorUsername(comment.getAuthor().getUsername());
        dto.setCreatedAt(comment.getCreatedAt());
        return dto;
    }
}