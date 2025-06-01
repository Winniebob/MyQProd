package com.videoplatform.dto;

import java.time.LocalDateTime;

public class SubscriptionDTO {
    private Long id;
    private String subscriberUsername;
    private String channelUsername;
    private String status;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public SubscriptionDTO() {}

    public static SubscriptionDTOBuilder builder() {
        return new SubscriptionDTOBuilder();
    }

    public Long getId() {
        return id;
    }

    public String getSubscriberUsername() {
        return subscriberUsername;
    }

    public String getChannelUsername() {
        return channelUsername;
    }

    public String getStatus() {
        return status;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setSubscriberUsername(String subscriberUsername) {
        this.subscriberUsername = subscriberUsername;
    }

    public void setChannelUsername(String channelUsername) {
        this.channelUsername = channelUsername;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public static class SubscriptionDTOBuilder {
        private final SubscriptionDTO instance = new SubscriptionDTO();

        public SubscriptionDTOBuilder id(Long id) {
            instance.setId(id);
            return this;
        }

        public SubscriptionDTOBuilder subscriberUsername(String subscriberUsername) {
            instance.setSubscriberUsername(subscriberUsername);
            return this;
        }

        public SubscriptionDTOBuilder channelUsername(String channelUsername) {
            instance.setChannelUsername(channelUsername);
            return this;
        }

        public SubscriptionDTOBuilder status(String status) {
            instance.setStatus(status);
            return this;
        }

        public SubscriptionDTOBuilder startDate(LocalDateTime startDate) {
            instance.setStartDate(startDate);
            return this;
        }

        public SubscriptionDTOBuilder endDate(LocalDateTime endDate) {
            instance.setEndDate(endDate);
            return this;
        }

        public SubscriptionDTO build() {
            return instance;
        }
    }
}