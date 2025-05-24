package com.videoplatform.controller;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentDTO addComment(@RequestBody CreateCommentRequest req, Principal principal) {
        return commentService.addComment(req, principal);
    }

    @GetMapping("/video/{videoId}")
    public List<CommentDTO> getCommentsByVideo(@PathVariable Long videoId) {
        return commentService.getCommentsTreeByVideoId(videoId);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long commentId, Principal principal) {
        commentService.softDeleteComment(commentId, principal.getName());
        return ResponseEntity.ok().build();
    }
    @PutMapping("/{commentId}")
    public CommentDTO updateComment(@PathVariable Long commentId,
                                    @RequestBody Map<String, String> body,
                                    Principal principal) {
        String newText = body.get("text");
        return commentService.updateComment(commentId, newText, principal.getName());
    }
}
