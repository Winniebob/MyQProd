package com.videoplatform.controller;

import com.videoplatform.dto.AddCardRequest;
import com.videoplatform.dto.PaymentMethodDTO;
import com.videoplatform.service.PaymentService;
import com.stripe.exception.StripeException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/payments/cards")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentMethodDTO> addCard(@Valid @RequestBody AddCardRequest req,
                                                    Principal principal) throws StripeException {
        return ResponseEntity.ok(paymentService.addCard(req, principal));
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodDTO>> listCards(Principal principal) {
        return ResponseEntity.ok(paymentService.listCards(principal));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeCard(@PathVariable Long id, Principal principal) throws StripeException {
        paymentService.removeCard(id, principal);
        return ResponseEntity.noContent().build();
    }
}
