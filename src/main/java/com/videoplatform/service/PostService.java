package com.videoplatform.service;

import com.videoplatform.dto.CreatePostRequest;
import com.videoplatform.dto.PostDTO;
import com.videoplatform.model.Post;
import com.videoplatform.model.User;
import com.videoplatform.repository.PostRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    private final Path postUploadPath = Paths.get("uploads/posts");

    @Transactional
    public PostDTO createPost(MultipartFile file, CreatePostRequest request, String username) {
        try {
            if (!Files.exists(postUploadPath)) {
                Files.createDirectories(postUploadPath);
            }
            String mediaUrl = null;
            if (file != null && !file.isEmpty()) {
                String original = file.getOriginalFilename();
                String filename = UUID.randomUUID() + "_" + original;
                Path target = postUploadPath.resolve(filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                mediaUrl = "/uploads/posts/" + filename;
            }

            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Post post = Post.builder()
                    .author(user)
                    .content(request.getContent())
                    .mediaUrl(mediaUrl)
                    .createdAt(LocalDateTime.now())
                    .build();
            post = postRepository.save(post);

            return mapToDto(post);
        } catch (IOException e) {
            throw new RuntimeException("Failed to store media file", e);
        }
    }

    public Page<PostDTO> getFeed(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToDto);
    }

    private PostDTO mapToDto(Post post) {
        return PostDTO.builder()
                .id(post.getId())
                .authorUsername(post.getAuthor().getUsername())
                .content(post.getContent())
                .mediaUrl(post.getMediaUrl())
                .createdAt(post.getCreatedAt())
                .build();
    }
}