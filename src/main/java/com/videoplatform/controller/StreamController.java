package com.videoplatform.controller;

import com.videoplatform.dto.StreamDTO;
import com.videoplatform.service.StreamService;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    @PostMapping
    public ResponseEntity<StreamDTO> create(@RequestParam @NotBlank String title,
                                            Principal principal) {
        return ResponseEntity.ok(streamService.createStream(title, principal));
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<StreamDTO> start(@PathVariable Long id,
                                           Principal principal) {
        return ResponseEntity.ok(streamService.startStream(id, principal));
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<StreamDTO> stop(@PathVariable Long id,
                                          Principal principal) {
        return ResponseEntity.ok(streamService.stopStream(id, principal));
    }

    @GetMapping("/active")
    public ResponseEntity<List<StreamDTO>> active() {
        return ResponseEntity.ok(streamService.getActiveStreams());
    }

    @GetMapping
    public ResponseEntity<List<StreamDTO>> all() {
        return ResponseEntity.ok(streamService.getAllStreams());
    }
}
