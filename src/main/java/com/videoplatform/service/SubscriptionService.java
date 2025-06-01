package com.videoplatform.service;

import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.SubscriptionEntity.SubscriptionStatus;
import com.videoplatform.model.User;
import com.videoplatform.repository.SubscriptionRepository;
import com.videoplatform.repository.UserRepository;
import com.videoplatform.dto.CreateSubscriptionRequestDTO;
import com.videoplatform.dto.SubscriptionDTO;
import com.videoplatform.model.Notification;
import com.stripe.exception.StripeException;
import com.stripe.model.Subscription;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;


    /**
     * Создаёт платную подписку через Stripe и сохраняет локально.
     *
     * @param requestDTO содержит channelId и priceId
     * @param principal  текущий аутентифицированный пользователь
     * @return SubscriptionDTO с данными подписки
     */
    @Transactional
    public SubscriptionDTO createPaidSubscription(CreateSubscriptionRequestDTO requestDTO, Principal principal) {
        User subscriber = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        User channel = userRepository.findById(requestDTO.getChannelId())
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        if (subscriber.getId().equals(channel.getId())) {
            throw new RuntimeException("Нельзя подписаться на себя");
        }

        // Проверяем, нет ли уже активной подписки у этого пользователя на данного автора
        if (subscriptionRepository.existsBySubscriberAndChannel(subscriber, channel)) {
            SubscriptionEntity existing = subscriptionRepository.findBySubscriberAndChannel(subscriber, channel)
                    .orElseThrow();
            if (existing.getStatus() == SubscriptionStatus.ACTIVE) {
                throw new RuntimeException("У вас уже активная подписка на этого автора");
            }
        }

        try {
            // 1) Создаём подписку в Stripe (только с указанием customer и price)
            SubscriptionCreateParams params = SubscriptionCreateParams.builder()
                    .setCustomer(subscriber.getStripeCustomerId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(requestDTO.getPriceId())
                            .build())
                    .build();

            // Импорт com.stripe.model.Subscription
            com.stripe.model.Subscription stripeSubscription =
                    com.stripe.model.Subscription.create(params);

            // 2) Вместо чтения текущих периодов, просто сохраняем startDate = LocalDateTime.now(),
            //    а endDate оставляем null. Фактическую дату окончания будем получать позже через вебхук.
            LocalDateTime startDate = LocalDateTime.now();

            SubscriptionEntity entity = SubscriptionEntity.builder()
                    .subscriber(subscriber)
                    .channel(channel)
                    .stripeSubscriptionId(stripeSubscription.getId())
                    .status(SubscriptionStatus.ACTIVE)
                    .startDate(startDate)
                    .endDate(null)   // пока неизвестно, получим позже из вебхука
                    .build();

            subscriptionRepository.save(entity);

            // 3) Отправляем уведомление автору, что на него оформили подписку
            notificationService.createNotification(
                    channel,
                    Notification.NotificationType.NEW_SUBSCRIPTION,
                    "Пользователь " + subscriber.getUsername() + " подписался на ваш канал",
                    null
            );

            // 4) Формируем и возвращаем DTO
            return SubscriptionDTO.builder()
                    .id(entity.getId())
                    .subscriberUsername(subscriber.getUsername())
                    .channelUsername(channel.getUsername())
                    .status(entity.getStatus().name())
                    .startDate(entity.getStartDate())
                    .endDate(entity.getEndDate())
                    .build();

        } catch (com.stripe.exception.StripeException e) {
            throw new RuntimeException("Ошибка Stripe при создании подписки: " + e.getMessage(), e);
        }
    }
    /**
     * Отменяет подписку как в Stripe, так и локально.
     *
     * @param subscriptionId id локальной SubscriptionEntity
     * @param principal      текущий пользователь
     */
    @Transactional
    public void cancelSubscription(Long subscriptionId, Principal principal) {
        SubscriptionEntity entity = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        User subscriber = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!entity.getSubscriber().getId().equals(subscriber.getId())) {
            throw new RuntimeException("Нет прав для отмены этой подписки");
        }

        try {
            // Отменяем в Stripe
            Subscription stripeSub = Subscription.retrieve(entity.getStripeSubscriptionId());
            stripeSub.cancel(); // отмена подписки

            // Обновляем локально статус и дату окончания
            entity.setStatus(SubscriptionStatus.CANCELED);
            entity.setEndDate(LocalDateTime.now());
            subscriptionRepository.save(entity);

            // Отправляем уведомление автору о том, что подписчик отменил подписку
            notificationService.createNotification(
                    entity.getChannel(),
                    Notification.NotificationType.NEW_SUBSCRIPTION,
                    "Пользователь " + subscriber.getUsername() + " отменил подписку на вашем канале",
                    null
            );

        } catch (StripeException e) {
            throw new RuntimeException("Ошибка Stripe при отмене подписки: " + e.getMessage(), e);
        }
    }

    /**
     * Получить список всех подписок текущего пользователя.
     */
    public List<SubscriptionDTO> getMySubscriptions(Principal principal) {
        User subscriber = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<SubscriptionEntity> subs = subscriptionRepository.findBySubscriberAndStatus(subscriber, SubscriptionStatus.ACTIVE);
        return subs.stream().map(entity -> SubscriptionDTO.builder()
                        .id(entity.getId())
                        .subscriberUsername(entity.getSubscriber().getUsername())
                        .channelUsername(entity.getChannel().getUsername())
                        .status(entity.getStatus().name())
                        .startDate(entity.getStartDate())
                        .endDate(entity.getEndDate())
                        .build())
                .toList();
    }

    /**
     * Получить список подписчиков (active) для данного канала.
     */
    public List<SubscriptionDTO> getChannelSubscribers(Long channelId) {
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new RuntimeException("Channel not found"));

        List<SubscriptionEntity> subs = subscriptionRepository.findByChannelAndStatus(channel, SubscriptionStatus.ACTIVE);
        return subs.stream().map(entity -> SubscriptionDTO.builder()
                        .id(entity.getId())
                        .subscriberUsername(entity.getSubscriber().getUsername())
                        .channelUsername(entity.getChannel().getUsername())
                        .status(entity.getStatus().name())
                        .startDate(entity.getStartDate())
                        .endDate(entity.getEndDate())
                        .build())
                .toList();
    }
    /**
     * Проверяет, имеет ли пользователь (username) активную подписку на channelId.
     */
    public boolean userIsSubscribed(String username, Long channelId) {
        User subscriber = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        User channel = userRepository.findById(channelId)
                .orElseThrow(() -> new IllegalArgumentException("Channel not found"));

        return subscriptionRepository.findBySubscriberAndChannel(subscriber, channel)
                .filter(sub -> sub.getStatus() == SubscriptionStatus.ACTIVE)
                .isPresent();
    }
}