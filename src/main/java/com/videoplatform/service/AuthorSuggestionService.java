package com.videoplatform.service;

import com.videoplatform.dto.UserProfileDTO;
import com.videoplatform.model.User;
import com.videoplatform.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthorSuggestionService {

    private final SubscriptionRepository subscriptionRepository;

    public List<UserProfileDTO> suggestAuthors(Principal principal) {
        // топ-10 по числу подписчиков
        List<User> top = subscriptionRepository.findTopCreators(PageRequest.of(0, 10));

        return top.stream().map(u ->
                UserProfileDTO.builder()
                        .id(u.getId())
                        .username(u.getUsername())
                        .displayName(u.getDisplayName())
                        .subscribersCount(subscriptionRepository.countByChannel(u))
                        .build()
        ).collect(Collectors.toList());
    }
}