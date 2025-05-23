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
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;

    @PostMapping("/")
    public ResponseEntity<CommentDTO> create(@RequestBody CreateCommentRequest req, Principal principal) {
        return ResponseEntity.ok(commentService.addComment(req, principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, Principal principal) {
        commentService.deleteComment(id, principal);
        return ResponseEntity.noContent().build();
    }

    // Пагинация только корневых комментариев
    @GetMapping("/video/{videoId}")
    public ResponseEntity<Page<CommentDTO>> listRoot(
            @PathVariable Long videoId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(commentService.listRootComments(videoId, page, size));
    }

    // Всё дерево комментариев
    @GetMapping("/video/{videoId}/tree")
    public ResponseEntity<List<CommentDTO>> listTree(@PathVariable Long videoId) {
        return ResponseEntity.ok(commentService.listCommentTree(videoId));
    }
}