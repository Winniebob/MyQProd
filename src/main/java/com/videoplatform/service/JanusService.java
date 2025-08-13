package com.videoplatform.service;

import com.videoplatform.controller.StreamController.JanusRoomResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class JanusService {

    @Value("${janus.rest.url:http://localhost:8088/janus}")
    private String janusRestUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Создаёт новую VideoRoom в Janus и возвращает её roomId (и, при необходимости, pin/secret).
     */
    public JanusRoomResponse createVideoRoom(Long streamId) {
        try {
            // 1) Создаём Janus session
            Map<String, Object> createSessionReq = Map.of(
                    "janus", "create",
                    "transaction", streamId.toString()
            );
            ResponseEntity<Map> sessionResp = restTemplate.postForEntity(
                    janusRestUrl, createSessionReq, Map.class
            );
            Map<String, Object> sessionBody = sessionResp.getBody();
            if (sessionBody == null || !sessionBody.containsKey("data")) {
                throw new IllegalStateException("Janus create session: empty body");
            }
            String sessionId = ((Map<String, Object>) sessionBody.get("data")).get("id").toString();

            // 2) Attach плагин videoroom
            String sessionUrl = janusRestUrl + "/" + sessionId;
            Map<String, Object> attachReq = Map.of(
                    "janus", "attach",
                    "plugin", "janus.plugin.videoroom",
                    "transaction", streamId.toString()
            );
            ResponseEntity<Map> attachResp = restTemplate.postForEntity(sessionUrl, attachReq, Map.class);
            Map<String, Object> attachBody = attachResp.getBody();
            if (attachBody == null || !attachBody.containsKey("data")) {
                throw new IllegalStateException("Janus attach: empty body");
            }
            String handleId = ((Map<String, Object>) attachBody.get("data")).get("id").toString();

            // 3) Создаём комнату VideoRoom (message ДОЛЖЕН идти на /{sessionId}/{handleId})
            long roomId = System.currentTimeMillis() & 0xfffffffL;
            Map<String, Object> body = new HashMap<>();
            body.put("request", "create");
            body.put("room", roomId);
            body.put("description", "Room for streamId=" + streamId);
            body.put("publishers", 4);

            Map<String, Object> createRoomReq = new HashMap<>();
            createRoomReq.put("janus", "message");
            createRoomReq.put("body", body);
            createRoomReq.put("transaction", streamId.toString());

            String handleUrl = sessionUrl + "/" + handleId;
            ResponseEntity<Map> roomResp = restTemplate.postForEntity(handleUrl, createRoomReq, Map.class);
            Map<String, Object> roomBody = roomResp.getBody();
            if (roomBody == null || !roomBody.containsKey("plugindata")) {
                throw new IllegalStateException("Janus create room: empty plugindata");
            }
            Map<String, Object> resultData =
                    (Map<String, Object>) ((Map<String, Object>) roomBody.get("plugindata")).get("data");

            JanusRoomResponse response = new JanusRoomResponse();
            response.setRoomId(String.valueOf(resultData.get("room")));
            if (resultData.containsKey("pin")) {
                response.setPin(String.valueOf(resultData.get("pin")));
            }
            if (resultData.containsKey("secret")) {
                response.setSecret(String.valueOf(resultData.get("secret")));
            }
            return response;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create Janus VideoRoom for streamId=" + streamId + ": " + e.getMessage(), e);
        }
    }
}