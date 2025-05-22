package com.videoplatform.repository;

import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    // === Добавляем для Stripe ===
    Optional<User> findByStripeCustomerId(String stripeCustomerId);

}