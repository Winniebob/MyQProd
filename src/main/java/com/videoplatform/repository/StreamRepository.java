package com.videoplatform.repository;

import com.videoplatform.model.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StreamRepository extends JpaRepository<Stream, Long> {
    Optional<Stream> findByStreamKey(String streamKey);
    List<Stream> findByIsLiveTrue();
}