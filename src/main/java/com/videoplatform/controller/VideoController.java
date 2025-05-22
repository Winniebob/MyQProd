package com.videoplatform.controller;

import com.videoplatform.dto.VideoDTO;
import com.videoplatform.model.Video;
import com.videoplatform.repository.VideoRepository;
import com.videoplatform.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.*;
import org.springframework.http.MediaTypeFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

    private final VideoRepository videoRepository;
    private final VideoService videoService;
    private final Path uploadDir = Paths.get("uploads");

    /**
     * Stream a video file with HTTP Range support, increment view count.
     */
    @GetMapping("/stream/{filename}")
    public ResponseEntity<FileSystemResource> streamVideo(@PathVariable String filename) {
        File file = uploadDir.resolve(filename).toFile();
        if (!file.exists() || !file.isFile()) {
            return ResponseEntity.notFound().build();
        }

        // Increment view counter
        videoService.incrementViewCount(filename);

        FileSystemResource resource = new FileSystemResource(file);
        MediaType mediaType = MediaTypeFactory.getMediaType(resource)
                .orElse(MediaType.APPLICATION_OCTET_STREAM);

        return ResponseEntity.ok()
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentLength(file.length())
                .contentType(mediaType)
                .body(resource);
    }

    /**
     * Get total view count for a given video ID.
     */
    @GetMapping("/{id}/views")
    public ResponseEntity<Integer> getViewCount(@PathVariable Long id) {
        return ResponseEntity.ok(videoService.getViewCount(id));
    }

    /**
     * Upload a new video.
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam(value = "isPublic", defaultValue = "true") boolean isPublic,
            Principal principal
    ) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Файл пустой");
        }
        videoService.uploadVideo(file, title, description, isPublic, principal);
        return ResponseEntity.ok("Видео успешно загружено");
    }

    /**
     * List all videos.
     */
    @GetMapping
    public ResponseEntity<List<VideoDTO>> getAllVideos() {
        List<VideoDTO> dtos = videoRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Search videos by title.
     */
    @GetMapping("/search")
    public ResponseEntity<List<VideoDTO>> searchVideos(@RequestParam("query") String query) {
        List<VideoDTO> dtos = videoRepository
                .findByTitleContainingIgnoreCase(query)
                .stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get the 10 most recent public videos for the carousel.
     */
    @GetMapping("/carousel")
    public ResponseEntity<List<VideoDTO>> getCarouselVideos() {
        List<VideoDTO> dtos = videoService.getVideosForCarousel().stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Get the top recommended videos.
     */
    @GetMapping("/recommended")
    public ResponseEntity<List<VideoDTO>> getRecommendedVideos() {
        List<VideoDTO> dtos = videoService.getRecommendedVideos().stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private VideoDTO mapToDto(Video video) {
        return VideoDTO.builder()
                .id(video.getId())
                .title(video.getTitle())
                .description(video.getDescription())
                .videoUrl(video.getVideoUrl())
                .thumbnailUrl(video.getThumbnailUrl())
                .isPublic(video.getIsPublic())
                .createdAt(video.getCreatedAt())
                .authorUsername(video.getUser().getUsername())
                .build();
    }
}