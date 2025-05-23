package com.videoplatform.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SubscriptionDTO {
    private Long id;
    private String subscriberUsername;
    private String channelUsername;
    private String status;          // ACTIVE или CANCELED
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}