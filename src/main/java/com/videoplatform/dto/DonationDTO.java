package com.videoplatform.dto;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DonationDTO {
    private Long id;
    private String fromUsername;
    private String toUsername;
    private BigDecimal amount;
    private String message;
    private LocalDateTime createdAt;
}