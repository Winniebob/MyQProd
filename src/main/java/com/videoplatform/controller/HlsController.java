package com.videoplatform.controller;

import com.videoplatform.model.Stream;
import com.videoplatform.service.HlsStorageService;
import com.videoplatform.service.StreamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/streams/hls")
@RequiredArgsConstructor
public class HlsController {

    private final HlsStorageService hlsStorageService;
    private final StreamService streamService;

    // Загрузка сегмента или плейлиста
    @PostMapping("/{streamId}/upload")
    public ResponseEntity<?> uploadSegment(@PathVariable Long streamId,
                                           @RequestParam("file") MultipartFile file,
                                           Principal principal) {
        try {
            // Проверка, что streamId принадлежит текущему пользователю
            Stream stream = streamService.getStreamById(streamId);
            if (!stream.getUser().getUsername().equals(principal.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Нет доступа к этому стриму");
            }

            hlsStorageService.saveSegment(streamId, file);
            return ResponseEntity.ok().build();
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Ошибка сохранения файла");
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Стрим не найден");
        }
    }
}