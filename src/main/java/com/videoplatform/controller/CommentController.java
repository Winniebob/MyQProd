package com.videoplatform.controller;

import com.videoplatform.dto.CommentDTO;
import com.videoplatform.dto.CreateCommentRequest;
import com.videoplatform.dto.UpdateCommentRequest;
import com.videoplatform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/")
    public ResponseEntity<CommentDTO> create(@RequestBody CreateCommentRequest req, Principal principal) {
        return ResponseEntity.ok(commentService.addComment(req, principal));
    }
    @PutMapping("/{id}")
    public ResponseEntity<CommentDTO> update(@PathVariable Long id,
                                             @RequestBody UpdateCommentRequest req,
                                             Principal principal) {
        return ResponseEntity.ok(commentService.updateComment(id, req, principal));
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        commentService.deleteComment(id, principal);
        return ResponseEntity.noContent().build();
    }
    @GetMapping("/video/{videoId}")
    public ResponseEntity<List<CommentDTO>> listTree(@PathVariable Long videoId) {
        return ResponseEntity.ok(commentService.listCommentTree(videoId));
    }
}