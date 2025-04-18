package com.workbridge.workbridge_app.mapper;

import com.workbridge.workbridge_app.dto.ChatMessageResponseDTO;
import com.workbridge.workbridge_app.entity.ChatMessage;

public class ChatMessageMapper {

    public static ChatMessageResponseDTO toDTO(ChatMessage message) {
        if (message == null) {
            return null;
        }

        return ChatMessageResponseDTO.builder()
                .id(message.getId())
                .senderUsername(message.getSender().getUsername())
                .recipientUsername(message.getRecipient().getUsername())
                .content(message.getContent())
                .timestamp(message.getTimestamp().toString())
                .deletedBySender(message.isDeletedBySender())
                .deletedByRecipient(message.isDeletedByRecipient())
                .build();
    }

}
