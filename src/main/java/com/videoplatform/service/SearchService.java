package com.videoplatform.service;

import com.videoplatform.search.PostDocument;
import com.videoplatform.search.VideoDocument;
import com.videoplatform.repository.PostRepository;
import com.videoplatform.repository.VideoRepository;
import com.videoplatform.search.PostSearchRepository;
import com.videoplatform.search.VideoSearchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final VideoRepository videoRepo;
    private final PostRepository postRepo;
    private final VideoSearchRepository videoSearchRepo;
    private final PostSearchRepository postSearchRepo;

    @PostConstruct
    public void initIndexes() {
        videoRepo.findAll().stream()
                .map(v -> VideoDocument.builder()
                        .id(v.getId())
                        .title(v.getTitle())
                        .description(v.getDescription())
                        .videoUrl(v.getVideoUrl())
                        .thumbnailUrl(v.getThumbnailUrl())
                        .isPublic(v.getIsPublic())
                        .build())
                .forEach(videoSearchRepo::save);

        postRepo.findAll().stream()
                .map(p -> PostDocument.builder()
                        .id(p.getId())
                        .content(p.getContent())
                        .mediaUrl(p.getMediaUrl())
                        .build())
                .forEach(postSearchRepo::save);
    }

    public List<VideoDocument> searchVideos(String query) {
        return videoSearchRepo.findByTitleContainingOrDescriptionContaining(query, query);
    }

    public List<PostDocument> searchPosts(String query) {
        return postSearchRepo.findByContentContaining(query);
    }

    public List<Object> searchAll(String query) {
        Stream<Object> vs = searchVideos(query).stream().map(v -> (Object) v);
        Stream<Object> ps = searchPosts(query).stream().map(p -> (Object) p);
        return Stream.concat(vs, ps).collect(Collectors.toList());
    }
}

