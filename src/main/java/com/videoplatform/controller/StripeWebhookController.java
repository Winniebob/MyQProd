package com.videoplatform.controller;

import com.videoplatform.service.StripeWebhookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;

import java.io.BufferedReader;
import java.io.IOException;

@RestController
@RequestMapping("/api/webhooks/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final StripeWebhookService webhookService;

    @PostMapping
    public ResponseEntity<Void> handle(
            @RequestHeader("Stripe-Signature") String sigHeader,
            HttpServletRequest request
    ) throws IOException {
        StringBuilder payload = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            reader.lines().forEach(payload::append);
        }
        webhookService.handleWebhook(payload.toString(), sigHeader);
        return ResponseEntity.ok().build();
    }
}
