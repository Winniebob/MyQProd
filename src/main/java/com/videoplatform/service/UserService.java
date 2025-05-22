package com.videoplatform.service;

import com.videoplatform.dto.UserProfileDTO;
import com.videoplatform.exception.NotFoundException;
import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.User;
import com.videoplatform.repository.SubscriptionRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * Возвращает DTO профиля пользователя:
     * – сам профиль
     * – число подписчиков
     * – флаг, подписан ли на него текущий юзер (если залогинен)
     */
    @Transactional(readOnly = true)
    public UserProfileDTO getUserProfile(Long channelId, Principal principal) {
        // 1) находим профиль
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 2) считаем подписчиков
        long subscribersCount = subscriptionRepository.countByChannel(channel);

        // 3) проверяем, подписан ли на этого канала текущий юзер
        boolean isSubscribed = false;
        if (principal != null) {
            User me = userRepository.findByUsername(principal.getName())
                    .orElseThrow(() -> new NotFoundException("Current user not found"));
            isSubscribed = subscriptionRepository
                    .existsBySubscriberAndChannel(me, channel);
        }

        // 4) собираем DTO
        return UserProfileDTO.builder()
                .id(channel.getId())
                .username(channel.getUsername())
                .displayName(channel.getDisplayName())
                .bio(channel.getBio())
                .avatarUrl(channel.getAvatarUrl())
                .coverUrl(channel.getCoverUrl())
                .createdAt(channel.getCreatedAt())
                .videos(/* TODO: сюда список видео DTO */ null)
                .subscribersCount(subscribersCount)
                .isSubscribed(isSubscribed)
                .build();
    }

    /**
     * Подписаться на канал channelId
     */
    @Transactional
    public void subscribe(Long channelId, Principal principal) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found"));
        // не даём подписаться на себя и дублировать подписку
        if (!me.getId().equals(channel.getId())
                && !subscriptionRepository.existsBySubscriberAndChannel(me, channel)) {
            SubscriptionEntity sub = SubscriptionEntity.builder()
                    .subscriber(me)
                    .channel(channel)
                    .build();
            subscriptionRepository.save(sub);
        }
    }

    /**
     * Отписаться от канала channelId
     */
    @Transactional
    public void unsubscribe(Long channelId, Principal principal) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new NotFoundException("User not found"));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new NotFoundException("Channel not found"));
        subscriptionRepository
                .findBySubscriberAndChannel(me, channel)
                .ifPresent(subscriptionRepository::delete);
    }
}