package com.videoplatform.service;

import com.videoplatform.controller.StreamController.JanusRoomResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class JanusService {

// Адрес вашего Janus REST API
 private final String janusRestUrl = "http://localhost:8088/janus";
private final RestTemplate restTemplate = new RestTemplate();
// Адрес Janus REST API (вынесен в конфиг), пример: http://localhost:8088/janus
   @org.springframework.beans.factory.annotation.Value("${janus.rest.url:http://localhost:8088/janus}")
 private String janusRestUrl;

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
                                long roomId = System.currentTimeMillis() & 0xfffffffL; // простой генератор ID
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
                       if (attachBody == null || !attachBody.containsKey("data")) {                throw new IllegalStateException("Janus attach: empty body");
                            }
                       String handleId = ((Map<String, Object>) attachBody.get("data")).get("id").toString();

                                // 3) Создаём комнату VideoRoom (отправляем message на /{sessionId}/{handleId})
                                        long roomId = System.currentTimeMillis() & 0xfffffffL; // простой генератор ID
                        Map<String, Object> body = new HashMap<>();
                        body.put("request", "create");
                        body.put("room", roomId);
                       body.put("description", "Room for streamId=" + streamId);           body.put("publishers", 4); // максимум одновременных публикаций

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
                       response.setRoomId(resultData.get("room").toString());
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