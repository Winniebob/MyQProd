package com.videoplatform.controller;

import com.videoplatform.dto.CreatePostRequest;
import com.videoplatform.dto.PostDTO;
import com.videoplatform.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ResponseEntity<PostDTO> createPost(
            @RequestParam(value = "file", required = false) MultipartFile file,
            @Valid @ModelAttribute CreatePostRequest request,
            @AuthenticationPrincipal UserDetails user
    ) {
        PostDTO dto = postService.createPost(file, request, user.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<Page<PostDTO>> getFeed(Pageable pageable) {
        return ResponseEntity.ok(postService.getFeed(pageable));
    }
}
