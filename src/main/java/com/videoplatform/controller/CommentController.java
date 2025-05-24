package com.videoplatform.controller;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public CommentDTO addComment(@RequestBody CreateCommentRequest req, Principal principal) {
        return commentService.addComment(req, principal);
    }

    @GetMapping("/video/{videoId}")
    public List<CommentDTO> getCommentsByVideo(@PathVariable Long videoId) {
        return commentService.getCommentsTreeByVideoId(videoId);
    }
}