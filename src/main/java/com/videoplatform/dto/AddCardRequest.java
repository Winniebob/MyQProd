package com.videoplatform.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class AddCardRequest {
    @NotBlank
    private String paymentMethodToken; // token from Stripe.js
}