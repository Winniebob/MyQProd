package com.videoplatform.dto;

public class LikeDTO {

    private Long entityId;
    private String entityType;
    private boolean liked;

    public LikeDTO() {}

    public LikeDTO(Long entityId, String entityType, boolean liked) {
        this.entityId = entityId;
        this.entityType = entityType;
        this.liked = liked;
    }

    public Long getEntityId() {
        return entityId;
    }

    public void setEntityId(Long entityId) {
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public void setEntityType(String entityType) {
        this.entityType = entityType;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
