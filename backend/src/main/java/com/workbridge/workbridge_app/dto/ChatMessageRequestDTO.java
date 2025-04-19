package com.workbridge.workbridge_app.dto;



import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChatMessageRequestDTO {
    private String senderUsername;
    private String recipientUsername;
    private String content;
    private String timestamp;
}

