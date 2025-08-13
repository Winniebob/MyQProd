package com.videoplatform.controller;

import com.videoplatform.dto.SignalMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebRtcSignalingController {

    private final SimpMessagingTemplate messagingTemplate;

    // Клиент шлёт на /app/signal
    @MessageMapping("/signal")
    public void signaling(@Payload SignalMessage signal) {
        // Сервер ретранслирует всем слушателям на стрим
        messagingTemplate.convertAndSend("/topic/signal/" + signal.getStreamKey(), signal);
        log.info("Signal: {} for streamKey {}", signal.getType(), signal.getStreamKey());
        try {
                       if (signal == null) {
                                log.warn("Ignored null SignalMessage");
                                return;
                            }             if (signal.getType() == null || signal.getStreamKey() == null || signal.getStreamKey().isBlank()) {
                                log.warn("Ignored invalid signal: type or streamKey is missing");
                                return;
                            }
                        // простая защита от слишком больших SDP/ICE сообщений
                                String payloadPreview = "";
                        try {
                                String sdp = (String) signal.getPayload();
                                if (sdp != null) {
                                       payloadPreview = sdp.substring(0, Math.min(120, sdp.length()));
                                    }
                            } catch (ClassCastException ignore) {
                                // payload не строка — не логируем содержимое
            }
                        messagingTemplate.convertAndSend("/topic/signal/" + signal.getStreamKey(), signal);
                        log.info("Signal [{}] forwarded to streamKey={}, payload[0..120]={}", signal.getType(), signal.getStreamKey(), payloadPreview);
                    } catch (Exception e) {
                       log.error("Failed to process signal message: {}", e.getMessage(), e);
                   }
    }
}