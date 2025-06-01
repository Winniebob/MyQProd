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
    }
}