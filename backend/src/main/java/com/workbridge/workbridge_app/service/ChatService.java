package com.workbridge.workbridge_app.service;

import com.workbridge.workbridge_app.dto.ChatMessageRequestDTO;
import com.workbridge.workbridge_app.dto.ChatMessageResponseDTO;
import com.workbridge.workbridge_app.entity.ApplicationUser;
import com.workbridge.workbridge_app.entity.ChatMessage;
import com.workbridge.workbridge_app.exception.MessageNotFoundException;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.mapper.ChatMessageMapper;
import com.workbridge.workbridge_app.repository.ChatMessageRepository;
import com.workbridge.workbridge_app.repository.UserRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    public void sendPrivateMessage(ChatMessageRequestDTO message) {

        ApplicationUser sender = userRepository.findByUsername(message.getSenderUsername()).orElseThrow(
                () -> new UserNotFoundException("Sender username not found: " + message.getSenderUsername()));

        ApplicationUser recipient = userRepository.findByUsername(message.getRecipientUsername()).orElseThrow(
                () -> new UserNotFoundException("Recipient username not found: " + message.getRecipientUsername()));

        ChatMessage entity = ChatMessage.builder().sender(sender).recipient(recipient).content(message.getContent())
                .timestamp(message.getTimestamp()).build();

        chatMessageRepository.save(entity);

        messagingTemplate.convertAndSend("/topic/users/" + message.getRecipientUsername(), message);
    }

    public List<ChatMessageResponseDTO> getMessages(String username) {
        ApplicationUser sender = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Sender username not found: " + username));
        List<ChatMessage> messages = chatMessageRepository.findBySenderOrRecipientOrderByTimestampAsc(sender, sender);

        return messages.stream().map(ChatMessageMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public void deleteConversationForUser(String currentUsername, String otherUsername) {

        ApplicationUser currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + currentUsername));

        ApplicationUser otherUser = userRepository.findByUsername(otherUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + otherUsername));

        List<ChatMessage> messages = chatMessageRepository.findVisibleMessagesBetween(currentUser, otherUser);

        for (ChatMessage msg : messages) {
            if (msg.getSender().equals(currentUser)) {
                msg.setDeletedBySender(true);
            }
            if (msg.getRecipient().equals(currentUser)) {
                msg.setDeletedByRecipient(true);
            }
        }

        chatMessageRepository.saveAll(messages);
    }

    @Transactional
    public void deleteMessage(Long messageId, String currentUsername, boolean deleteForAll) {

        ApplicationUser currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuário não encontrado: " + currentUsername));

        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new MessageNotFoundException("Mensagem não encontrada"));

        if (deleteForAll) {
            message.setDeletedBySender(true);
            message.setDeletedByRecipient(true);
        } else {

            if (message.getSender().equals(currentUser)) {
                message.setDeletedBySender(true);
            } else if (message.getRecipient().equals(currentUser)) {
                message.setDeletedByRecipient(true);
            } else {
                throw new RuntimeException("Usuário não autorizado");
            }
        }

        chatMessageRepository.save(message);
    }

}
