package com.videoplatform.controller;

import com.videoplatform.model.Stream;
import com.videoplatform.service.HlsStorageService;
import com.videoplatform.service.StreamService;
import com.videoplatform.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.Principal;

/**
 * Контроллер отдаёт HLS-плейлист и сегменты,
 * проверяя доступ: если стрим не публичный, пользователь должен быть подписан.
 */
@RestController
@RequestMapping("/streams/hls")
@RequiredArgsConstructor
public class HlsStreamController {

    private final HlsStorageService hlsStorageService;
    private final StreamService streamService;
    private final SubscriptionService subscriptionService;

    /**
     * Отдаёт файл HLS (index.m3u8 или сегмент .ts).
     * URL: GET /streams/hls/{streamId}/{filename}
     */
    @GetMapping("/{streamId}/{filename:.+}")
    public ResponseEntity<Resource> serveHlsFile(
            @PathVariable Long streamId,
            @PathVariable String filename,
            Principal principal
    ) throws IOException {
        Stream stream = streamService.getStreamById(streamId);

        // Если стрим не публичный, проверяем подписку
        if (!stream.isPublic()) {
            if (principal == null) {
                return ResponseEntity.status(401).build();
            }
            boolean subscribed = subscriptionService.userIsSubscribed(
                    principal.getName(),
                    stream.getUser().getId()
            );
            if (!subscribed) {
                return ResponseEntity.status(403).build();
            }
        }

        Resource file = hlsStorageService.loadHlsFile(streamId, filename);

        String contentType = Files.probeContentType(Path.of(file.getURI()));
        if (contentType == null) {
            if (filename.endsWith(".m3u8")) {
                contentType = "application/vnd.apple.mpegurl";
            } else if (filename.endsWith(".ts")) {
                contentType = "video/MP2T";
            } else {
                contentType = "application/octet-stream";
            }
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                .body(file);
    }
}