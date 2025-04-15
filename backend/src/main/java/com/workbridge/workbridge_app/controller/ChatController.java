package com.workbridge.workbridge_app.controller;

import com.workbridge.workbridge_app.dto.ChatMessage;
import com.workbridge.workbridge_app.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessage message) {
        log.info("Mensagem recebida de {} para {}: {}", message.getSenderId(), message.getRecipientId(), message.getContent());
        chatService.sendPrivateMessage(message);
    }
}
