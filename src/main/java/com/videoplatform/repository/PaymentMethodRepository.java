package com.videoplatform.repository;

import com.videoplatform.model.PaymentMethodEntity;
import com.videoplatform.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PaymentMethodRepository extends JpaRepository<PaymentMethodEntity, Long> {
    List<PaymentMethodEntity> findByUser(User user);
    void deleteByStripePaymentMethodId(String stripePaymentMethodId);
}