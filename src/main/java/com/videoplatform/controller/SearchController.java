package com.videoplatform.controller;

import com.videoplatform.search.VideoDocument;
import com.videoplatform.search.PostDocument;
import com.videoplatform.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping("/videos")
    public ResponseEntity<List<VideoDocument>> searchVideos(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchVideos(query));
    }

    @GetMapping("/posts")
    public ResponseEntity<List<PostDocument>> searchPosts(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchPosts(query));
    }

    @GetMapping
    public ResponseEntity<List<Object>> searchAll(@RequestParam String query) {
        return ResponseEntity.ok(searchService.searchAll(query));
    }
}
