package com.workbridge.workbridge_app.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatMessage {
    private String senderId;
    private String recipientId;
    private String content;
    private String timestamp;
}

