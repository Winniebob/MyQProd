package com.videoplatform.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import java.util.List;

public interface VideoSearchRepository extends ElasticsearchRepository<VideoDocument, Long> {
    List<VideoDocument> findByTitleContainingOrDescriptionContaining(String title, String description);
}