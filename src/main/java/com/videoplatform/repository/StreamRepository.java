package com.videoplatform.repository;

import com.videoplatform.model.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {

    Optional<Stream> findByStreamKey(String streamKey);

    List<Stream> findByIsLiveTrue();

    List<Stream> findByUser(com.videoplatform.model.User user);

    List<Stream> findByStatus(Stream.StreamStatus status);

}