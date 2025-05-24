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

    @PostMapping("/create")
    public ResponseEntity<Stream> createStream(@RequestParam String title,
                                               @RequestParam(required = false) String description,
                                               Principal principal) {
        Stream stream = streamService.createStream(title, description, principal);
        return ResponseEntity.ok(stream);
    }

    @PostMapping("/{id}/start")
    public ResponseEntity<Stream> startStream(@PathVariable Long id, Principal principal) {
        Stream stream = streamService.startStream(id, principal);
        return ResponseEntity.ok(stream);
    }

    @PostMapping("/{id}/stop")
    public ResponseEntity<Stream> stopStream(@PathVariable Long id, Principal principal) {
        Stream stream = streamService.stopStream(id, principal);
        return ResponseEntity.ok(stream);
    }

    @GetMapping("/my")
    public ResponseEntity<List<Stream>> getUserStreams(Principal principal) {
        List<Stream> streams = streamService.getUserStreams(principal);
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/live")
    public ResponseEntity<List<Stream>> getLiveStreams() {
        List<Stream> streams = streamService.getLiveStreams();
        return ResponseEntity.ok(streams);
    }

    @GetMapping("/key/{streamKey}")
    public ResponseEntity<Stream> getStreamByKey(@PathVariable String streamKey) {
        return streamService.getStreamByKey(streamKey)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}