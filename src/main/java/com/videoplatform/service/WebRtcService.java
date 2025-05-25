package com.videoplatform.service;

import com.videoplatform.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class WebRtcService {

    private final Map<String, WebRtcSession> sessions = new ConcurrentHashMap<>();

    public String createSession(Long streamId, User user) {
        String sessionId = UUID.randomUUID().toString();
        WebRtcSession session = new WebRtcSession(sessionId, streamId, user, LocalDateTime.now());
        sessions.put(sessionId, session);
        return sessionId;
    }

    public void closeSession(String sessionId) {
        sessions.remove(sessionId);
    }

    private static class WebRtcSession {
        private final String sessionId;
        private final Long streamId;
        private final User user;
        private final LocalDateTime createdAt;

        public WebRtcSession(String sessionId, Long streamId, User user, LocalDateTime createdAt) {
            this.sessionId = sessionId;
            this.streamId = streamId;
            this.user = user;
            this.createdAt = createdAt;
        }

        // getters if needed
    }
}