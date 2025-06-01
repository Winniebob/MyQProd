package com.videoplatform.controller;

import com.videoplatform.service.StreamService;
import com.videoplatform.service.SubscriptionService;
import com.videoplatform.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/hls")
@RequiredArgsConstructor
public class HlsTokenController {

    private final StreamService streamService;
    private final SubscriptionService subscriptionService;
    private final JwtUtils jwtUtils;

    @Value("${antmedia.host}")
    private String antmediaHost;

    @Value("${antmedia.app-name}")
    private String antmediaApp;

    /**
     * Возвращает подписанный HLS-URL:
     * GET /api/hls/{streamId}/token
     */
    @GetMapping("/{streamId}/token")
    public ResponseEntity<Map<String, String>> getHlsUrl(
            @PathVariable Long streamId,
            Principal principal) {

        var stream = streamService.getStreamById(streamId);

        if (!stream.isPublic()) {
            if (principal == null) {
                return ResponseEntity.status(401).build();
            }
            boolean subscribed = subscriptionService.userIsSubscribed(
                    principal.getName(), stream.getUser().getId());
            if (!subscribed) {
                return ResponseEntity.status(403).build();
            }
        }

        // Генерируем JWT-токен
        String token = jwtUtils.generateStreamToken(streamId);
        // Формируем формат URL для Ant Media HLS
        String hlsUrl = String.format(
                "http://%s:5080/%s/streams/%d/playlist.m3u8?token=%s",
                antmediaHost, antmediaApp, streamId, token);

        return ResponseEntity.ok(Map.of("hlsUrl", hlsUrl));
    }
}