package com.videoplatform.dto;

public class CreateSubscriptionRequestDTO {
    private Long channelId;
    private String priceId;

    public CreateSubscriptionRequestDTO() {}

    public CreateSubscriptionRequestDTO(Long channelId, String priceId) {
        this.channelId = channelId;
        this.priceId = priceId;
    }

    public Long getChannelId() {
        return channelId;
    }

    public void setChannelId(Long channelId) {
        this.channelId = channelId;
    }

    public String getPriceId() {
        return priceId;
    }

    public void setPriceId(String priceId) {
        this.priceId = priceId;
    }
}