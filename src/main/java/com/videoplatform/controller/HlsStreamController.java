package com.videoplatform.controller;

import com.videoplatform.service.HlsStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;

@RestController
@RequestMapping("/streams")
@RequiredArgsConstructor
public class HlsStreamController {

    private final HlsStorageService hlsStorageService;

    @GetMapping("/{streamId}/hls/{filename:.+}")
    public ResponseEntity<Resource> getSegment(@PathVariable Long streamId,
                                               @PathVariable String filename) {
        try {
            Path filePath = hlsStorageService.getSegmentPath(streamId, filename);
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/vnd.apple.mpegurl";
            if (filename.endsWith(".ts")) {
                contentType = "video/MP2T";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}