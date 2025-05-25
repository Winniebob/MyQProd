package com.videoplatform.service;

import com.videoplatform.model.Stream;
import com.videoplatform.model.User;
import com.videoplatform.repository.StreamRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class StreamService {

    private final StreamRepository streamRepository;
    private final UserRepository userRepository;
    private final WebRtcService webRtcService;

    public Stream createStream(String title, String description, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        Stream stream = Stream.builder()
                .user(user)
                .title(title)
                .description(description)
                .streamKey(generateStreamKey())
                .status(Stream.StreamStatus.CREATED)
                .isLive(false)
                .build();

        return streamRepository.save(stream);
    }

    public Stream startStream(Long streamId, Principal principal) {
        Stream stream = getStreamById(streamId);
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!stream.getUser().equals(user)) {
            throw new SecurityException("No permission to start this stream");
        }

        stream.setStatus(Stream.StreamStatus.LIVE);
        stream.setIsLive(true);
        stream.setStartedAt(LocalDateTime.now());
        stream.setStreamUrl(generateStreamUrl(stream));

        return streamRepository.save(stream);
    }

    public Stream stopStream(Long streamId, Principal principal) {
        Stream stream = getStreamById(streamId);
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        if (!stream.getUser().equals(user)) {
            throw new SecurityException("No permission to stop this stream");
        }

        stream.setStatus(Stream.StreamStatus.STOPPED);
        stream.setIsLive(false);
        stream.setStoppedAt(LocalDateTime.now());
        stream.setRecordingUrl(generateRecordingUrl(stream));

        return streamRepository.save(stream);
    }

    public List<Stream> getUserStreams(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NoSuchElementException("User not found"));
        return streamRepository.findByUser(user);
    }

    public List<Stream> getLiveStreams() {
        return streamRepository.findByIsLiveTrue();
    }

    public Stream getStreamById(Long id) {
        return streamRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Stream not found"));
    }

    public Stream getStreamByKey(String streamKey) {
        return streamRepository.findByStreamKey(streamKey)
                .orElseThrow(() -> new NoSuchElementException("Stream not found"));
    }

    private String generateStreamKey() {
        return UUID.randomUUID().toString();
    }

    private String generateStreamUrl(Stream stream) {
        return "/streams/live/" + stream.getId() + "/index.m3u8";
    }

    private String generateRecordingUrl(Stream stream) {
        return "/streams/recordings/" + stream.getId() + ".mp4";
    }
}