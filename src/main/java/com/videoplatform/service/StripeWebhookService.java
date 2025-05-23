package com.videoplatform.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.Subscription;
import com.stripe.net.Webhook;
import com.videoplatform.exception.NotFoundException;
import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.SubscriptionEntity.SubscriptionStatus;
import com.videoplatform.model.User;
import com.videoplatform.repository.SubscriptionRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Основной метод обработки любого Stripe-webhook'а.
     */
    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe signature", e);
        }

        switch (event.getType()) {
            case "customer.subscription.created":
                onCreated(event);
                break;
            case "customer.subscription.updated":
                onUpdated(event);
                break;
            case "customer.subscription.deleted":
                onDeleted(event);
                break;
            default:
                // всё остальное — игнорируем
        }
    }

    @Transactional
    protected void onCreated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        User user = userRepository.findByStripeCustomerId(stripeSub.getCustomer())
                .orElseThrow(() -> new NotFoundException("User not found"));

        JsonNode data = parseRawJson(event);
        long startEpoch = data.path("current_period_start").asLong(0L);
        long endEpoch   = data.path("current_period_end").  asLong(0L);

        SubscriptionEntity sub = SubscriptionEntity.builder()
                .subscriber(user)
                .channel(user)  // ваша логика: чей канал
                .stripeSubscriptionId(stripeSub.getId())
                .status(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()))
                .startDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(startEpoch),
                        ZoneId.systemDefault()))
                .endDate(  LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpoch),
                        ZoneId.systemDefault()))
                .build();

        subscriptionRepository.save(sub);
    }

    @Transactional
    protected void onUpdated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()));

                    JsonNode data = parseRawJson(event);
                    long endEpoch = data.path("current_period_end").asLong(0L);
                    sub.setEndDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpoch),
                            ZoneId.systemDefault()));

                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    protected void onDeleted(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(subscriptionRepository::delete);
    }

    /**
     * Извлекает объект Subscription из Event — без Optional.flatMap.
     */
    private Subscription extractSubscription(Event event) {
        EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
        if (deser == null) return null;
        Object obj = deser.getObject().orElse(null);
        return (obj instanceof Subscription) ? (Subscription) obj : null;
    }

    /**
     * Берёт «сырую» JSON-строку из webhook-пакета и парсит её в JsonNode.
     */
    private JsonNode parseRawJson(Event event) {
        EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
        String raw = "{}";
        if (deser != null && deser.getRawJson() != null) {
            raw = deser.getRawJson();
        }
        try {
            return objectMapper.readTree(raw);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse Stripe webhook JSON", e);
        }
    }
}