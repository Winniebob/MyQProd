package com.videoplatform.controller;

import com.videoplatform.dto.SubscriptionDTO;
import com.videoplatform.model.SubscriptionEntity;
import com.videoplatform.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    public ResponseEntity<List<SubscriptionDTO>> getMySubscriptions(
            Principal principal,
            @RequestParam(value = "status", required = false) SubscriptionEntity.SubscriptionStatus status
    ) {
        List<SubscriptionDTO> dtos = subscriptionService.listMySubscriptions(principal, status);
        return ResponseEntity.ok(dtos);
    }

    @GetMapping("/subscribers")
    public ResponseEntity<List<SubscriptionDTO>> getMySubscribers(
            Principal principal,
            @RequestParam(value = "status", required = false) SubscriptionEntity.SubscriptionStatus status
    ) {
        List<SubscriptionDTO> dtos = subscriptionService.listMySubscribers(principal, status);
        return ResponseEntity.ok(dtos);
    }
}
