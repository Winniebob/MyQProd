package com.videoplatform.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DonateRequest {
    private Long toUserId;
    private BigDecimal amount;
    private String message;
}
