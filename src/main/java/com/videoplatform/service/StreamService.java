package com.videoplatform.service;

import com.videoplatform.dto.StreamDTO;
import com.videoplatform.model.Stream;
import com.videoplatform.model.User;
import com.videoplatform.repository.StreamRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepo;
    private final UserRepository userRepo;

    @Transactional
    public StreamDTO createStream(String title, Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        Stream stream = Stream.builder()
                .user(user)
                .title(title)
                .streamKey(key)
                .isLive(false)
                .build();
        stream = streamRepo.save(stream);
        return mapToDto(stream);
    }

    @Transactional
    public StreamDTO startStream(Long id, Principal principal) {
        Stream stream = streamRepo.findById(id).orElseThrow();
        if (!stream.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("Нет прав на управление этим потоком");
        }
        stream.setIsLive(true);
        stream.setStartedAt(LocalDateTime.now());
        streamRepo.save(stream);
        return mapToDto(stream);
    }

    @Transactional
    public StreamDTO stopStream(Long id, Principal principal) {
        Stream stream = streamRepo.findById(id).orElseThrow();
        if (!stream.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("Нет прав на управление этим потоком");
        }
        stream.setIsLive(false);
        stream.setEndedAt(LocalDateTime.now());
        streamRepo.save(stream);
        return mapToDto(stream);
    }

    public List<StreamDTO> getActiveStreams() {
        return streamRepo.findByIsLiveTrue().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<StreamDTO> getAllStreams() {
        return streamRepo.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private StreamDTO mapToDto(Stream s) {
        return StreamDTO.builder()
                .id(s.getId())
                .title(s.getTitle())
                .streamKey(s.getStreamKey())
                .isLive(s.getIsLive())
                .startedAt(s.getStartedAt())
                .endedAt(s.getEndedAt())
                .authorUsername(s.getUser().getUsername())
                .build();
    }
}