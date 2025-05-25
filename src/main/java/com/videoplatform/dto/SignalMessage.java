package com.videoplatform.dto;

import lombok.Data;

@Data
public class SignalMessage {
    private String streamKey;
    private String type; // offer, answer, candidate
    private String sdp;
    private String candidate;
    private String from; // можно добавить имя пользователя или id
}