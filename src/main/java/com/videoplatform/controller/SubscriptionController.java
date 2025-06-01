package com.videoplatform.controller;

import com.videoplatform.dto.CreateSubscriptionRequestDTO;
import com.videoplatform.dto.SubscriptionDTO;
import com.videoplatform.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    /**
     * Создать платную подписку.
     * Пример: POST /api/subscriptions/create
     * Тело (JSON):
     * {
     *   "channelId": 5,
     *   "priceId": "price_1JNZWb2eZvKYlo2C6o7QhV69"
     * }
     */
    @PostMapping("/create")
    public ResponseEntity<SubscriptionDTO> createSubscription(
            @RequestBody CreateSubscriptionRequestDTO requestDTO,
            Principal principal) {

        SubscriptionDTO dto = subscriptionService.createPaidSubscription(requestDTO, principal);
        return ResponseEntity.ok(dto);
    }

    /**
     * Отменить подписку.
     * Пример: DELETE /api/subscriptions/{id}/cancel
     */
    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelSubscription(
            @PathVariable Long id,
            Principal principal) {

        subscriptionService.cancelSubscription(id, principal);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить мои активные подписки.
     * Пример: GET /api/subscriptions/me
     */
    @GetMapping("/me")
    public ResponseEntity<List<SubscriptionDTO>> getMySubscriptions(Principal principal) {
        List<SubscriptionDTO> list = subscriptionService.getMySubscriptions(principal);
        return ResponseEntity.ok(list);
    }

    /**
     * Получить подписчиков указанного канала (по ID канала).
     * Пример: GET /api/subscriptions/channel/5/subscribers
     */
    @GetMapping("/channel/{channelId}/subscribers")
    public ResponseEntity<List<SubscriptionDTO>> getChannelSubscribers(@PathVariable Long channelId) {
        List<SubscriptionDTO> list = subscriptionService.getChannelSubscribers(channelId);
        return ResponseEntity.ok(list);
    }
}