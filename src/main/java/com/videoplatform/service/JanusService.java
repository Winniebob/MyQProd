
package com.videoplatform.service;

import com.videoplatform.controller.StreamController.JanusRoomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с REST API Janus (плагин VideoRoom).
 */
@Service
public class JanusService {

    // Адрес вашего Janus REST API
    private final String janusRestUrl = "http://localhost:8088/janus";
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Создаёт новую VideoRoom в Janus и возвращает её roomId (и, при необходимости, pin/secret).
     */
    public JanusRoomResponse createVideoRoom(Long streamId) {
        // 1) Создаём Janus session
        Map<String, Object> createSessionReq = Map.of(
                "janus", "create",
                "transaction", streamId.toString()
        );
        ResponseEntity<Map> sessionResp = restTemplate.postForEntity(
                janusRestUrl, createSessionReq, Map.class
        );
        Map<String, Object> sessionBody = sessionResp.getBody();
        String sessionId = ((Map<String, Object>) sessionBody.get("data")).get("id").toString();

        // 2) Attach плагин videoroom
        String attachUrl = janusRestUrl + "/" + sessionId;
        Map<String, Object> attachReq = Map.of(
                "janus", "attach",
                "plugin", "janus.plugin.videoroom",
                "transaction", streamId.toString()
        );
        ResponseEntity<Map> attachResp = restTemplate.postForEntity(attachUrl, attachReq, Map.class);
        String handleId = ((Map<String, Object>) attachResp.getBody().get("data")).get("id").toString();

        // 3) Создаём саму комнату VideoRoom
        long roomId = System.currentTimeMillis() & 0xfffffff; // простой генератор ID
        Map<String, Object> body = new HashMap<>();
        body.put("request", "create");
        body.put("room", roomId);
        body.put("description", "Room for streamId=" + streamId);
        body.put("publishers", 4); // максимум одновременных публикаций

        Map<String, Object> createRoomReq = new HashMap<>();
        createRoomReq.put("janus", "message");
        createRoomReq.put("body", body);
        createRoomReq.put("transaction", streamId.toString());
        createRoomReq.put("handle_id", handleId);

        ResponseEntity<Map> roomResp = restTemplate.postForEntity(attachUrl, createRoomReq, Map.class);
        Map<String, Object> resultData =
                (Map<String, Object>) ((Map<String, Object>) roomResp.getBody().get("plugindata")).get("data");

        JanusRoomResponse response = new JanusRoomResponse();
        response.setRoomId(resultData.get("room").toString());
        if (resultData.containsKey("pin")) {
            response.setPin(resultData.get("pin").toString());
        }
        if (resultData.containsKey("secret")) {
            response.setSecret(resultData.get("secret").toString());
        }
        return response;
    }
}