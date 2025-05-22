package com.videoplatform.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.videoplatform.dto.AddCardRequest;
import com.videoplatform.dto.PaymentMethodDTO;
import com.videoplatform.model.PaymentMethodEntity;
import com.videoplatform.model.User;
import com.videoplatform.repository.PaymentMethodRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaymentService {
    @Value("${stripe.api.key}")
    private String stripeApiKey;

    private final UserRepository userRepo;
    private final PaymentMethodRepository pmRepo;

    @Transactional
    public PaymentMethodDTO addCard(AddCardRequest request, Principal principal) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        // Create Stripe customer if necessary
        if (user.getStripeCustomerId() == null) {
            CustomerCreateParams params = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getUsername())
                    .build();
            Customer customer = Customer.create(params);
            user.setStripeCustomerId(customer.getId());
            userRepo.save(user);
        }

        // Attach payment method to customer
        PaymentMethod pm = PaymentMethod.retrieve(request.getPaymentMethodToken());
        pm.attach(PaymentMethodAttachParams.builder()
                .setCustomer(user.getStripeCustomerId())
                .build());

        // Save in our DB
        PaymentMethodEntity entity = PaymentMethodEntity.builder()
                .user(user)
                .stripePaymentMethodId(pm.getId())
                .cardBrand(pm.getCard().getBrand())
                .last4(pm.getCard().getLast4())
                // приводим Long → Integer
                .expMonth(Math.toIntExact(pm.getCard().getExpMonth()))
                .expYear(Math.toIntExact(pm.getCard().getExpYear()))
                .build();
        pmRepo.save(entity);

        return mapToDto(entity);
    }

    public List<PaymentMethodDTO> listCards(Principal principal) {
        User user = userRepo.findByUsername(principal.getName()).orElseThrow();
        return pmRepo.findByUser(user).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void removeCard(Long id, Principal principal) throws StripeException {
        Stripe.apiKey = stripeApiKey;

        PaymentMethodEntity entity = pmRepo.findById(id).orElseThrow();
        if (!entity.getUser().getUsername().equals(principal.getName())) {
            throw new RuntimeException("No rights");
        }
        // Detach from Stripe
        PaymentMethod pm = PaymentMethod.retrieve(entity.getStripePaymentMethodId());
        pm.detach();

        pmRepo.delete(entity);
    }

    private PaymentMethodDTO mapToDto(PaymentMethodEntity e) {
        return PaymentMethodDTO.builder()
                .id(e.getId())
                .stripePaymentMethodId(e.getStripePaymentMethodId())
                .cardBrand(e.getCardBrand())
                .last4(e.getLast4())
                .expMonth(e.getExpMonth())
                .expYear(e.getExpYear())
                .createdAt(e.getCreatedAt())
                .build();
    }
}