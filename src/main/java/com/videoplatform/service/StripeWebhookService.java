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

// ========================================================================
// TODO:
//   1) В application.yml или application.properties указать реальный секрет вебхука:
//        stripe.webhook.secret=ваш_секрет_из_Dashboard
//   2) В контроллере (например, StripeWebhookController) пробросить payload и sigHeader сюда:
//        stripeWebhookService.handleWebhook(payload, sigHeader);
//   3) Зарегистрировать в Stripe Dashboard URL: https://<ваш-домен>/api/stripe-webhook
//   4) Убедиться, что слушаются именно те event’ы, которые нужны (customer.subscription.created/updated/deleted).
// ========================================================================

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
     * @param payload   тело запроса (JSON от Stripe)
     * @param sigHeader заголовок "Stripe-Signature"
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
                // остальные типы событий игнорируем
        }
    }

    /**
     * Обработчик события создания подписки в Stripe.
     */
    @Transactional
    protected void onCreated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        // Извлекаем подписчика по stripeCustomerId
        User subscriber = userRepository.findByStripeCustomerId(stripeSub.getCustomer())
                .orElseThrow(() -> new NotFoundException("User not found for customer: " + stripeSub.getCustomer()));

        // Определяем канал, на который подписываются.
        // Если в вашей бизнес-логике канал = тот же пользователь, оставляем так;
        // иначе замените на логику получения канала из metadata или другого поля.
        User channel = subscriber;

        // Парсим «сырый» JSON объекта Subscription из Webhook, чтобы получить нужные поля
        JsonNode rawData = parseRawJson(event);
        long startEpoch = rawData.path("current_period_start").asLong(0L);
        long endEpoch   = rawData.path("current_period_end").asLong(0L);

        LocalDateTime startDate = LocalDateTime.ofInstant(Instant.ofEpochSecond(startEpoch), ZoneId.systemDefault());
        LocalDateTime endDate   = LocalDateTime.ofInstant(Instant.ofEpochSecond(endEpoch),   ZoneId.systemDefault());

        // Статус из Stripe приходит в нижнем регистре, приводим к вашему enum’у
        SubscriptionStatus status;
        try {
            status = SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase());
        } catch (IllegalArgumentException ex) {
            // Если статус не совпал с вашим enum, ставим по умолчанию ACTIVE
            status = SubscriptionStatus.ACTIVE;
        }

        SubscriptionEntity subscriptionEntity = SubscriptionEntity.builder()
                .subscriber(subscriber)
                .channel(channel)
                .stripeSubscriptionId(stripeSub.getId())
                .status(status)
                .startDate(startDate)
                .endDate(endDate)
                .build();

        subscriptionRepository.save(subscriptionEntity);
    }

    /**
     * Обработчик события изменения подписки в Stripe.
     */
    @Transactional
    protected void onUpdated(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(entity -> {
                    // Обновляем статус
                    try {
                        entity.setStatus(SubscriptionStatus.valueOf(stripeSub.getStatus().toUpperCase()));
                    } catch (IllegalArgumentException ignored) {
                        // Если статус не сопоставим, оставляем прежний
                    }

                    // Парсим «сырый» JSON объекта Subscription, чтобы обновить endDate
                    JsonNode rawData = parseRawJson(event);
                    long endEpoch = rawData.path("current_period_end").asLong(0L);
                    if (endEpoch > 0) {
                        entity.setEndDate(LocalDateTime.ofInstant(
                                Instant.ofEpochSecond(endEpoch),
                                ZoneId.systemDefault()));
                    }

                    subscriptionRepository.save(entity);
                });
    }

    /**
     * Обработчик события удаления подписки в Stripe.
     */
    @Transactional
    protected void onDeleted(Event event) {
        Subscription stripeSub = extractSubscription(event);
        if (stripeSub == null) return;

        subscriptionRepository.findByStripeSubscriptionId(stripeSub.getId())
                .ifPresent(subscriptionRepository::delete);
    }

    /**
     * Извлекает объект Subscription из переданного Event.
     */
    private Subscription extractSubscription(Event event) {
        EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
        if (deser == null) {
            return null;
        }
        Object obj = deser.getObject().orElse(null);
        return (obj instanceof Subscription) ? (Subscription) obj : null;
    }

    /**
     * Берёт «сырую» JSON-строку из webhook-пакета и парсит её в JsonNode.
     * Метод возвращает JSON только data.object, чтобы мы могли читать поля внутри Subscription.
     */
    private JsonNode parseRawJson(Event event) {
        EventDataObjectDeserializer deser = event.getDataObjectDeserializer();
        String rawJson = "{}";
        if (deser != null && deser.getRawJson() != null) {
            rawJson = deser.getRawJson();
        }
        try {
            return objectMapper.readTree(rawJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Cannot parse Stripe webhook JSON", e);
        }
    }
}