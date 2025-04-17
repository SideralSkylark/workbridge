package com.workbridge.workbridge_app.service;

import com.workbridge.workbridge_app.entity.ChatMessage;
import com.workbridge.workbridge_app.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public void sendPrivateMessage(ChatMessage message) {
        ChatMessage entity = ChatMessage.builder()
                .senderUsername(message.getSenderUsername())
                .recipientUsername(message.getRecipientUsername())
                .content(message.getContent())
                .timestamp(message.getTimestamp())
                .build();

        chatMessageRepository.save(entity);
        // envia para o usuário destinatário diretamente
        messagingTemplate.convertAndSend("/topic/users/" + message.getRecipientUsername(), message);
    }
}
