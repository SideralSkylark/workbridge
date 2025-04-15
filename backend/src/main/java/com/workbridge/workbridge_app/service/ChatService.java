package com.workbridge.workbridge_app.service;

import com.workbridge.workbridge_app.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendPrivateMessage(ChatMessage message) {
        // envia para o usuário destinatário diretamente
        messagingTemplate.convertAndSend("/topic/users/" + message.getRecipientId(), message);
    }
}
