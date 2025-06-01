package com.videoplatform.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class HlsStorageService {
    // Корневой путь хранения HLS (указать в application.yml)
    private final Path rootLocation = Paths.get("/path/to/hls-storage");
    private final Path hlsBasePath = Paths.get("storage/hls");

    public Path getStreamFolder(Long streamId) {
        return hlsBasePath.resolve(String.valueOf(streamId));
    }

    public void saveSegment(Long streamId, MultipartFile file) throws IOException {
        Path streamFolder = getStreamFolder(streamId);
        if (!Files.exists(streamFolder)) {
            Files.createDirectories(streamFolder);
        }
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        Path targetPath = streamFolder.resolve(filename);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
    }



    // Scheduled cleanup to delete files older than 10 minutes
    @Scheduled(fixedDelay = 600000)
    public void cleanupOldSegments() {
        try {
            Files.walk(hlsBasePath)
                    .filter(Files::isRegularFile)
                    .filter(this::isFileOlderThan10Minutes)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            log.info("Deleted old HLS segment: {}", path);
                        } catch (IOException e) {
                            log.error("Error deleting file: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("Error walking HLS storage directory", e);
        }
    }

    private boolean isFileOlderThan10Minutes(Path path) {
        try {
            FileTime lastModifiedTime = Files.getLastModifiedTime(path);
            return lastModifiedTime.toInstant().isBefore(Instant.now().minus(Duration.ofMinutes(10)));
        } catch (IOException e) {
            log.error("Failed to get last modified time for {}", path, e);
            return false;
        }
    }
    public Resource loadHlsFile(Long streamId, String filename) {
        try {
            Path file = rootLocation.resolve(streamId.toString()).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("HLS file not found or not readable: " + filename);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid HLS file path: " + filename, e);
        }
    }
}