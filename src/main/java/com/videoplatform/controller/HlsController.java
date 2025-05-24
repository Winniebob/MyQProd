package com.videoplatform.controller;

import com.videoplatform.service.HlsStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/streams/hls")
@RequiredArgsConstructor
public class HlsController {

    private final HlsStorageService hlsStorageService;

    // Загрузка сегмента или плейлиста
    @PostMapping("/{streamId}/upload")
    public ResponseEntity<?> uploadSegment(@PathVariable Long streamId,
                                           @RequestParam("file") MultipartFile file) {
        try {
            hlsStorageService.saveSegment(streamId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(500).body("Ошибка сохранения файла");
        }
    }
}