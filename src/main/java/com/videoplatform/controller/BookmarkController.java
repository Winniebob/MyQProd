package com.videoplatform.controller;

import com.videoplatform.model.Bookmark;
import com.videoplatform.service.BookmarkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookmarks")
@RequiredArgsConstructor
public class BookmarkController {

    private final BookmarkService service;

    @PostMapping
    public ResponseEntity<?> add(@RequestParam Bookmark.TargetType type,
                                 @RequestParam Long targetId,
                                 @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        service.addBookmark(type, targetId, user.getUsername());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping
    public ResponseEntity<?> remove(@RequestParam Bookmark.TargetType type,
                                    @RequestParam Long targetId,
                                    @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        service.removeBookmark(type, targetId, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Bookmark> list(@RequestParam Bookmark.TargetType type,
                               @AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return service.listBookmarks(type, user.getUsername());
    }
}