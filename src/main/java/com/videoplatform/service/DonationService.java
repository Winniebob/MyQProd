package com.videoplatform.service;

import com.videoplatform.dto.DonationDTO;
import com.videoplatform.model.Donation;
import com.videoplatform.model.Notification;
import com.videoplatform.model.User;
import com.videoplatform.repository.DonationRepository;
import com.videoplatform.repository.NotificationRepository;
import com.videoplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DonationService {

    private final DonationRepository donationRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public DonationDTO donate(Long toUserId, java.math.BigDecimal amount, String message, String fromUsername) {
        User fromUser = userRepository.findByUsername(fromUsername)
                .orElseThrow(() -> new RuntimeException("Sender not found"));
        User toUser = userRepository.findById(toUserId)
                .orElseThrow(() -> new RuntimeException("Recipient not found"));

        Donation donation = Donation.builder()
                .fromUser(fromUser)
                .toUser(toUser)
                .amount(amount)
                .message(message)
                .build();
        donation = donationRepository.save(donation);

        // Создаем уведомление о донате
        Notification notification = new Notification();
        notification.setRecipient(toUser);
        notification.setMessage(String.format("Пользователь %s сделал донат %s", fromUser.getUsername(), amount.toString()));
        notification.setRead(false);
        notificationRepository.save(notification);

        return mapToDto(donation);
    }

    public List<DonationDTO> getReceivedDonations(String username) {
        User toUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return donationRepository.findByToUser(toUser).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private DonationDTO mapToDto(Donation d) {
        return DonationDTO.builder()
                .id(d.getId())
                .fromUsername(d.getFromUser().getUsername())
                .toUsername(d.getToUser().getUsername())
                .amount(d.getAmount())
                .message(d.getMessage())
                .createdAt(d.getCreatedAt())
                .build();
    }
}