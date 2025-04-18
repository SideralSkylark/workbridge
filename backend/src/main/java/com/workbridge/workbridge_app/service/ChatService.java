package com.workbridge.workbridge_app.service;

import com.workbridge.workbridge_app.entity.ChatMessage;
import com.workbridge.workbridge_app.repository.ChatMessageRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;

    public void sendPrivateMessage(ChatMessage message) {
        ChatMessage entity = ChatMessage.builder().senderUsername(message.getSenderUsername())
                .recipientUsername(message.getRecipientUsername()).content(message.getContent())
                .timestamp(message.getTimestamp()).build();

        chatMessageRepository.save(entity);
        // envia para o usuário destinatário diretamente
        messagingTemplate.convertAndSend("/topic/users/" + message.getRecipientUsername(), message);
    }

    public List<ChatMessage> getMessages(String username) {
        return chatMessageRepository.findBySenderUsernameOrRecipientUsernameOrderByTimestampAsc(username, username);
    }

    @Transactional
    public void deleteConversationForUser(String currentUsername, String otherUsername) {
        List<ChatMessage> messages = chatMessageRepository
                .findBySenderUsernameOrRecipientUsernameOrderByTimestampAsc(currentUsername, otherUsername);

        for (ChatMessage msg : messages) {
            if (msg.getSenderUsername().equals(currentUsername)) {
                msg.setDeletedBySender(true);
            }
            if (msg.getRecipientUsername().equals(currentUsername)) {
                msg.setDeletedByRecipient(true);
            }
        }

        chatMessageRepository.saveAll(messages);
    }

    @Transactional
    public void deleteMessage(Long messageId, String currentUser, boolean deleteForAll) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Mensagem não encontrada"));

        if (deleteForAll) {
            message.setDeletedBySender(true);
            message.setDeletedByRecipient(true);
        } else {
            if (message.getSenderUsername().equals(currentUser)) {
                message.setDeletedBySender(true);
            } else if (message.getRecipientUsername().equals(currentUser)) {
                message.setDeletedByRecipient(true);
            } else {
                throw new RuntimeException("Usuário não autorizado");
            }
        }

        chatMessageRepository.save(message);
    }

}
