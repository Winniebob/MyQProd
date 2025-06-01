package com.videoplatform.controller;

import com.videoplatform.model.Stream;
import com.videoplatform.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
public class StreamController {

    private final StreamService streamService;

    /**
     * Создать одиночный стрим.
     * Пример запроса: POST /api/streams/create?title=MyTitle&description=MyDescription
     */
    @PostMapping("/create")
    public ResponseEntity<Stream> createStream(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            Principal principal) {

        Stream stream = streamService.createStream(title, description, principal);
        return ResponseEntity.ok(stream);
    }

    /**
     * Запустить одиночный стрим.
     * Пример запроса: POST /api/streams/{id}/start
     */
    @PostMapping("/{id}/start")
    public ResponseEntity<Stream> startStream(
            @PathVariable Long id,
            Principal principal) {

        Stream stream = streamService.startStream(id, principal);
        return ResponseEntity.ok(stream);
    }

    /**
     * Остановить одиночный стрим.
     * Пример запроса: POST /api/streams/{id}/stop
     */
    @PostMapping("/{id}/stop")
    public ResponseEntity<Stream> stopStream(
            @PathVariable Long id,
            Principal principal) {

        Stream stream = streamService.stopStream(id, principal);
        return ResponseEntity.ok(stream);
    }

    /**
     * Получить все активные (LIVE) стримы.
     * Пример запроса: GET /api/streams/live
     */
    @GetMapping("/live")
    public ResponseEntity<List<Stream>> getLiveStreams() {
        List<Stream> live = streamService.getLiveStreams();
        return ResponseEntity.ok(live);
    }

    /**
     * Получить все стримы текущего пользователя.
     * Пример запроса: GET /api/streams/user
     */
    @GetMapping("/user")
    public ResponseEntity<List<Stream>> getUserStreams(Principal principal) {
        List<Stream> streams = streamService.getUserStreams(principal);
        return ResponseEntity.ok(streams);
    }

    /**
     * Создать групповой стрим.
     * Пример тела запроса (JSON):
     * {
     *   "title": "Group Title",
     *   "description": "Group Desc",
     *   "participantIds": [2,3,4],
     *   "isPublic": true,
     *   "groupName": "Fun Group"
     * }
     */
    @PostMapping("/create-group")
    public ResponseEntity<Stream> createGroupStream(
            @RequestParam String title,
            @RequestParam(required = false) String description,
            @RequestParam List<Long> participantIds,
            @RequestParam(defaultValue = "false") boolean isPublic,
            @RequestParam String groupName,
            Principal principal) {

        Stream stream = streamService.createGroupStream(title, description, participantIds, isPublic, groupName, principal);
        return ResponseEntity.ok(stream);
    }

    /**
     * Запустить групповой стрим.
     * Пример запроса: POST /api/streams/{id}/start-group
     */
    @PostMapping("/{id}/start-group")
    public ResponseEntity<Stream> startGroupStream(
            @PathVariable Long id,
            Principal principal) {

        Stream stream = streamService.startGroupStream(id, principal);
        return ResponseEntity.ok(stream);
    }
}