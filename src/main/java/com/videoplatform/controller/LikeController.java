package com.videoplatform.controller;

import com.videoplatform.dto.LikeResponse;
import com.videoplatform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    /**
     * /api/videos/{id}/like  — переключает лайк для текущего пользователя
     */
    @PostMapping("/{id}/like")
    public ResponseEntity<LikeResponse> toggleLike(
            @PathVariable("id") Long videoId,
            Principal principal
    ) {
        LikeResponse resp = likeService.toggleLike(videoId, principal);
        return ResponseEntity.ok(resp);
    }
}
