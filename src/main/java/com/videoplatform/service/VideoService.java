package com.videoplatform.service;

import com.videoplatform.model.User;
import com.videoplatform.model.Video;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.repository.VideoRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    private final VideoRepository videoRepository;
    private final UserRepository userRepository;

    private final Path uploadPath = Paths.get("uploads");
    @Transactional
    public void incrementViewCount(String filename) {
        String url = "/uploads/" + filename;
        videoRepository.findByVideoUrl(url).ifPresent(video -> {
            video.setViews(video.getViews() + 1);
            videoRepository.save(video);
        });
    }

    public int getViewCount(Long videoId) {
        return videoRepository.findById(videoId)
                .map(Video::getViews)
                .orElse(0);
    }

    public void generateThumbnail(Path videoPath, Path thumbnailPath) {
        try {
            String[] command = {
                    "ffmpeg", "-i", videoPath.toString(),
                    "-vf", "thumbnail,scale=320:240",
                    "-frames:v", "1",
                    thumbnailPath.toString()
            };

            Process process = new ProcessBuilder(command).inheritIO().start();
            process.waitFor();

        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error generating thumbnail", e);
        }
    }

    @Transactional
    public void uploadVideo(MultipartFile file, String title, String description, boolean isPublic, Principal principal) {
        try {
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFilename = file.getOriginalFilename();
            String filename = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = uploadPath.resolve(filename);

            while (Files.exists(filePath)) {
                filename = UUID.randomUUID() + "_" + originalFilename;
                filePath = uploadPath.resolve(filename);
            }

            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            String username = principal.getName();
            User user = userRepository.findByUsername(username).orElseThrow();

            String thumbnailFilename = filename + "_thumb.jpg";
            Path thumbnailPath = uploadPath.resolve(thumbnailFilename);
            generateThumbnail(filePath, thumbnailPath);

            Video video = Video.builder()
                    .user(user)
                    .title(title)
                    .description(description)
                    .videoUrl("/uploads/" + filename)
                    .thumbnailUrl("/uploads/" + thumbnailFilename)
                    .isPublic(isPublic)
                    .createdAt(LocalDateTime.now())
                    .build();

            videoRepository.save(video);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload video", e);
        }
    }

    public List<Video> getVideosForCarousel() {
        return videoRepository.findTop10ByIsPublicTrueOrderByCreatedAtDesc();
    }

    public List<Video> getRecommendedVideos() {
        return videoRepository.findTop10ByIsRecommendedTrueOrderByViewsDesc();
    }
}