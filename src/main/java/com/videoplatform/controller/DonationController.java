package com.videoplatform.controller;

import com.videoplatform.dto.DonationDTO;
import com.videoplatform.dto.DonateRequest;
import com.videoplatform.service.DonationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
public class DonationController {

    private final DonationService donationService;

    @PostMapping
    public ResponseEntity<DonationDTO> donate(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
                                              @RequestBody DonateRequest request) {
        DonationDTO dto = donationService.donate(request.getToUserId(), request.getAmount(), request.getMessage(), user.getUsername());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public ResponseEntity<List<DonationDTO>> getDonations(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        return ResponseEntity.ok(donationService.getReceivedDonations(user.getUsername()));
    }
}
