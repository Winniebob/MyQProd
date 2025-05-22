package com.videoplatform.controller;

import com.videoplatform.dto.UserProfileDTO;
import com.videoplatform.service.AuthorSuggestionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final AuthorSuggestionService suggestionService;

    @GetMapping("/authors")
    public ResponseEntity<List<UserProfileDTO>> getSuggestions(Principal principal) {
        return ResponseEntity.ok(suggestionService.suggestAuthors(principal));
    }
}