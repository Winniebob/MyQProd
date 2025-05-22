package com.videoplatform.dto;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentMethodDTO {
    private Long id;
    private String stripePaymentMethodId;
    private String cardBrand;
    private String last4;
    private Integer expMonth;
    private Integer expYear;
    private LocalDateTime createdAt;
}
