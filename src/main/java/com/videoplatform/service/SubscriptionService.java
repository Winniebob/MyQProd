package com.videoplatform.service;

import com.videoplatform.dto.SubscriptionDTO;
import com.videoplatform.exception.ForbiddenException;
import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.User;
import com.videoplatform.repository.SubscriptionRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

    public List<SubscriptionDTO> listMySubscriptions(Principal principal, SubscriptionEntity.SubscriptionStatus status) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<SubscriptionEntity> subs = (status == null)
                ? subscriptionRepository.findBySubscriber(me)
                : subscriptionRepository.findBySubscriberAndStatus(me, status);
        return subs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public List<SubscriptionDTO> listMySubscribers(Principal principal, SubscriptionEntity.SubscriptionStatus status) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<SubscriptionEntity> subs = (status == null)
                ? subscriptionRepository.findByChannel(me)
                : subscriptionRepository.findByChannelAndStatus(me, status);
        return subs.stream().map(this::mapToDto).collect(Collectors.toList());
    }

    @Transactional
    public void subscribe(Long channelId, Principal principal) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (me.getId().equals(channel.getId()) ||
                subscriptionRepository.existsBySubscriberAndChannel(me, channel)) {
            return;
        }

        SubscriptionEntity sub = SubscriptionEntity.builder()
                .subscriber(me)
                .channel(channel)
                .status(SubscriptionEntity.SubscriptionStatus.ACTIVE)
                .build();
        subscriptionRepository.save(sub);
    }

    @Transactional
    public void unsubscribe(Long channelId, Principal principal) {
        User me = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));
        subscriptionRepository.findBySubscriberAndChannel(me, channel)
                .ifPresent(subscriptionRepository::delete);
    }

    private SubscriptionDTO mapToDto(SubscriptionEntity sub) {
        return SubscriptionDTO.builder()
                .id(sub.getId())
                .subscriberUsername(sub.getSubscriber().getUsername())
                .channelUsername(sub.getChannel().getUsername())
                .status(sub.getStatus().name())
                .startDate(sub.getStartDate())
                .endDate(sub.getEndDate())
                .build();
    }
}