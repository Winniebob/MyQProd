package com.videoplatform.controller;

import com.videoplatform.dto.LikeDTO;
import com.videoplatform.service.LikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/add")
    public ResponseEntity<?> addLike(@RequestParam Long entityId,
                                     @RequestParam String entityType,
                                     Principal principal) {
        boolean added = likeService.addLike(entityId, entityType, principal);
        if (!added) {
            return ResponseEntity.badRequest().body("Вы уже поставили лайк");
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeLike(@RequestParam Long entityId,
                                        @RequestParam String entityType,
                                        Principal principal) {
        boolean removed = likeService.removeLike(entityId, entityType, principal);
        if (!removed) {
            return ResponseEntity.badRequest().body("Лайк не найден");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countLikes(@RequestParam Long entityId,
                                           @RequestParam String entityType) {
        long count = likeService.countLikes(entityId, entityType);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/status")
    public ResponseEntity<LikeDTO> checkLikeStatus(@RequestParam Long entityId,
                                                   @RequestParam String entityType,
                                                   Principal principal) {
        boolean liked = likeService.hasUserLiked(entityId, entityType, principal);
        LikeDTO dto = new LikeDTO(entityId, entityType, liked);
        return ResponseEntity.ok(dto);
    }
}