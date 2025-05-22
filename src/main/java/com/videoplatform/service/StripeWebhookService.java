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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StripeWebhookService {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public void handleWebhook(String payload, String sigHeader) {
        Event event;
        try {
            // проверяем подпись
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Invalid Stripe signature", e);
        }

        switch (event.getType()) {
            case "customer.subscription.created" -> onSubscriptionCreated(event);
            case "customer.subscription.updated" -> onSubscriptionUpdated(event);
            case "customer.subscription.deleted" -> onSubscriptionDeleted(event);
            default -> {
                // остальные события пока игнорируем
            }
        }
    }

    /**
     * Берёт «сырую» JSON-строку из EventDataObjectDeserializer и парсит её в JsonNode.
     */
    private JsonNode parseRawJson(Event event) {
        EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
        // если десериализатор вдруг null или вернул null JSON — подставляем пустой объект
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

    @Transactional
    protected void onSubscriptionCreated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        // 2) Находим нашего User
        User user = userRepository.findByStripeCustomerId(stripeSub.getCustomer())
                .orElseThrow(() -> new NotFoundException("User not found"));

        // 3) Берём “сырую” JSON-строку и разбираем её
        JsonNode node = parseRawJson(event);
        long epochStart = node.path("current_period_start").asLong(0L);
        long epochEnd = node.path("current_period_end").asLong(0L);

        // 4) Сохраняем нашу сущность
        SubscriptionEntity sub = SubscriptionEntity.builder()
                .subscriber(user)
                .channel(user)  // <-- ваша логика: на чей канал подписка
                .stripeSubscriptionId(stripeSub.getId())
                .status(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()))
                .startDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(epochStart), ZoneId.systemDefault()))
                .endDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(epochEnd), ZoneId.systemDefault()))
                .build();
        subscriptionRepository.save(sub);
    }

    @Transactional
    protected void onSubscriptionUpdated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(sub -> {
                    sub.setStatus(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()));

                    JsonNode node = parseRawJson(event);
                    long epochEnd = node.path("current_period_end").asLong(0L);
                    sub.setEndDate(LocalDateTime.ofInstant(Instant.ofEpochSecond(epochEnd), ZoneId.systemDefault()));

                    subscriptionRepository.save(sub);
                });
    }

    @Transactional
    protected void onSubscriptionDeleted(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(subscriptionRepository::delete);
    }

    /**
     * Безопасно достаёт Stripe Subscription из Event
     */
    private Subscription extractSubscription(Event event) {
        return Optional.ofNullable(event.getDataObjectDeserializer())
                .flatMap(d -> d.getObject().filter(o -> o instanceof Subscription).map(o -> (Subscription) o))
                .orElse(null);
    }
}
