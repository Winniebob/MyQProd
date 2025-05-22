package com.videoplatform.repository;

import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    // 1) Поиск подписки по stripeSubscriptionId
    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    // 2) Все подписки конкретного пользователя
    List<SubscriptionEntity> findBySubscriber(User subscriber);

    // 3) Все подписки конкретного пользователя с определённым статусом
    List<SubscriptionEntity> findBySubscriberAndStatus(User subscriber, SubscriptionEntity.SubscriptionStatus status);

    // 4) Все подписчики конкретного канала
    List<SubscriptionEntity> findByChannel(User channel);

    // 5) Все подписчики конкретного канала с определённым статусом
    List<SubscriptionEntity> findByChannelAndStatus(User channel, SubscriptionEntity.SubscriptionStatus status);

    // 6) Существует ли подписка между двумя User’ами
    boolean existsBySubscriberAndChannel(User subscriber, User channel);

    // 7) Найти именно одну подписку между двумя User’ами
    Optional<SubscriptionEntity> findBySubscriberAndChannel(User subscriber, User channel);

    // 8) Топ-10 самых популярных каналов
    @Query("SELECT s.channel FROM SubscriptionEntity s GROUP BY s.channel ORDER BY COUNT(s) DESC")
    List<User> findTopCreators(Pageable pageable);

    // 9) Утилита для топ-10
    default List<User> findTopCreators() {
        return findTopCreators(Pageable.ofSize(10));
    }
    long countByChannel(User channel);


}