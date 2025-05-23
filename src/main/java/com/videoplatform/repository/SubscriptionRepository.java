package com.videoplatform.repository;

import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.model.SubscriptionEntity.SubscriptionStatus;
import com.videoplatform.model.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<SubscriptionEntity, Long> {

    boolean existsBySubscriberAndChannel(User subscriber, User channel);

    Optional<SubscriptionEntity> findByStripeSubscriptionId(String stripeId);
    Optional<SubscriptionEntity> findBySubscriberAndChannel(User subscriber, User channel);

    long countByChannel(User channel);

    List<SubscriptionEntity> findBySubscriber(User subscriber);

    List<SubscriptionEntity> findBySubscriberAndStatus(User subscriber, SubscriptionStatus status);

    List<SubscriptionEntity> findByChannel(User channel);

    List<SubscriptionEntity> findByChannelAndStatus(User channel, SubscriptionStatus status);

    @Query("SELECT s.channel FROM SubscriptionEntity s GROUP BY s.channel ORDER BY COUNT(s) DESC")
    List<User> findTopCreators(Pageable pageable);


}