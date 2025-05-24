package com.videoplatform.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;

@Service
public class HlsStorageService {

    @Value("${app.hls.storage-path}")
    private String hlsStoragePath;

    public Path getStreamFolder(Long streamId) {
        return Paths.get(hlsStoragePath, String.valueOf(streamId));
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

    public Path getSegmentPath(Long streamId, String filename) {
        return getStreamFolder(streamId).resolve(filename);
    }
}