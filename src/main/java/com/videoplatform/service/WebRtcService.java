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

    public WebRtcSession getBySessionId(String sessionId) {
        return sessions.get(sessionId);
    }

    public void closeAllByStreamId(Long streamId) {
        sessions.entrySet().removeIf(e -> e.getValue().getStreamId().equals(streamId));
    }

    public int countByStreamId(Long streamId) {
        return (int) sessions.values().stream().filter(s -> s.getStreamId().equals(streamId)).count();
    }

    /** Удаление «протухших» сессий старше ttlMinutes. */
    public int cleanupExpired(int ttlMinutes) {
        LocalDateTime cutoff = LocalDateTime.now().minusMinutes(ttlMinutes);
        int before = sessions.size();
        sessions.entrySet().removeIf(e -> e.getValue().getCreatedAt().isBefore(cutoff));
        return before - sessions.size();
    }

    public static class WebRtcSession {
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

        public String getSessionId() { return sessionId; }
        public Long getStreamId() { return streamId; }
        public User getUser() { return user; }
        public LocalDateTime getCreatedAt() { return createdAt; }
    }
}