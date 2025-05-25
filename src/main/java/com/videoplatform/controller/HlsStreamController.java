package com.videoplatform.controller;

import com.videoplatform.service.HlsStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/streams/hls")
@RequiredArgsConstructor
public class HlsStreamController {

    private final HlsStorageService hlsStorageService;

    @GetMapping("/{streamId}/{filename:.+}")
    public ResponseEntity<Resource> serveHlsFile(@PathVariable Long streamId,
                                                 @PathVariable String filename) throws IOException {
        Resource file = hlsStorageService.loadHlsFile(streamId, filename);

        String contentType = Files.probeContentType(Path.of(file.getURI()));
        if (contentType == null) {
            contentType = "application/octet-stream";
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(file);
    }
}