package com.workbridge.workbridge_app.unit.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.workbridge.workbridge_app.chat.dto.ChatMessageRequestDTO;
import com.workbridge.workbridge_app.chat.dto.ChatMessageResponseDTO;
import com.workbridge.workbridge_app.chat.entity.ChatMessage;
import com.workbridge.workbridge_app.chat.exception.MessageNotFoundException;
import com.workbridge.workbridge_app.chat.repository.ChatMessageRepository;
import com.workbridge.workbridge_app.chat.service.ChatService;
import com.workbridge.workbridge_app.user.entity.ApplicationUser;
import com.workbridge.workbridge_app.user.exception.UserNotFoundException;
import com.workbridge.workbridge_app.user.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ChatService chatService;

    private ApplicationUser testUser;
    private ApplicationUser testRecipient;
    private ChatMessage testMessage;
    private ChatMessageRequestDTO messageRequest;
    private String currentTimestamp;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = new ApplicationUser();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        // Create test recipient
        testRecipient = new ApplicationUser();
        testRecipient.setId(2L);
        testRecipient.setUsername("recipient");
        testRecipient.setEmail("recipient@example.com");

        // Create current timestamp
        currentTimestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // Create test message
        testMessage = ChatMessage.builder()
            .id(1L)
            .sender(testUser)
            .recipient(testRecipient)
            .content("Hello!")
            .timestamp(currentTimestamp)
            .build();

        // Create test message request
        messageRequest = new ChatMessageRequestDTO();
        messageRequest.setSenderUsername("testuser");
        messageRequest.setRecipientUsername("recipient");
        messageRequest.setContent("Hello!");
        messageRequest.setTimestamp(currentTimestamp);
    }
/*
    @Test
    void sendPrivateMessage_WhenValidRequest_ShouldSendMessage() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("recipient")).thenReturn(Optional.of(testRecipient));
        when(chatMessageRepository.save(any(ChatMessage.class))).thenReturn(testMessage);

        // Act
        chatService.sendPrivateMessage(messageRequest);

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByUsername("recipient");
        verify(chatMessageRepository).save(any(ChatMessage.class));
        verify(messagingTemplate).convertAndSendToUser(eq("recipient"), eq("/queue/messages"), eq(messageRequest));
    }
 */
    @Test
    void sendPrivateMessage_WhenSenderNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        messageRequest.setSenderUsername("nonexistent");
        assertThrows(UserNotFoundException.class, () -> 
            chatService.sendPrivateMessage(messageRequest)
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).findByUsername("recipient");
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void sendPrivateMessage_WhenRecipientNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        messageRequest.setRecipientUsername("nonexistent");
        assertThrows(UserNotFoundException.class, () -> 
            chatService.sendPrivateMessage(messageRequest)
        );
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByUsername("nonexistent");
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }

    @Test
    void getMessages_WhenUserExists_ShouldReturnMessages() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findBySenderOrRecipientOrderByTimestampAsc(testUser, testUser))
            .thenReturn(Arrays.asList(testMessage));

        // Act
        List<ChatMessageResponseDTO> result = chatService.getMessages("testuser");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMessage.getContent(), result.get(0).getContent());
        verify(userRepository).findByUsername("testuser");
        verify(chatMessageRepository).findBySenderOrRecipientOrderByTimestampAsc(testUser, testUser);
    }

    @Test
    void getMessages_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            chatService.getMessages("nonexistent")
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(chatMessageRepository, never()).findBySenderOrRecipientOrderByTimestampAsc(any(), any());
    }

    @Test
    void deleteConversationForUser_WhenValidRequest_ShouldDeleteConversation() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(userRepository.findByUsername("recipient")).thenReturn(Optional.of(testRecipient));
        when(chatMessageRepository.findVisibleMessagesBetween(testUser, testRecipient))
            .thenReturn(Arrays.asList(testMessage));

        // Act
        chatService.deleteConversationForUser("testuser", "recipient");

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(userRepository).findByUsername("recipient");
        verify(chatMessageRepository).findVisibleMessagesBetween(testUser, testRecipient);
        verify(chatMessageRepository).saveAll(anyList());
    }

    @Test
    void deleteConversationForUser_WhenUserNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> 
            chatService.deleteConversationForUser("nonexistent", "recipient")
        );
        verify(userRepository).findByUsername("nonexistent");
        verify(userRepository, never()).findByUsername("recipient");
        verify(chatMessageRepository, never()).findVisibleMessagesBetween(any(), any());
        verify(chatMessageRepository, never()).saveAll(anyList());
    }

    @Test
    void deleteMessage_WhenValidRequest_ShouldDeleteMessage() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act
        chatService.deleteMessage(1L, "testuser", false);

        // Assert
        verify(userRepository).findByUsername("testuser");
        verify(chatMessageRepository).findById(1L);
        verify(chatMessageRepository).save(testMessage);
    }

    @Test
    void deleteMessage_WhenMessageNotFound_ShouldThrowException() {
        // Arrange
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(chatMessageRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(MessageNotFoundException.class, () -> 
            chatService.deleteMessage(999L, "testuser", false)
        );
        verify(userRepository).findByUsername("testuser");
        verify(chatMessageRepository).findById(999L);
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }

    @Test
    void deleteMessage_WhenUserNotAuthorized_ShouldThrowException() {
        // Arrange
        ApplicationUser unauthorizedUser = new ApplicationUser();
        unauthorizedUser.setId(3L);
        unauthorizedUser.setUsername("unauthorized");
        when(userRepository.findByUsername("unauthorized")).thenReturn(Optional.of(unauthorizedUser));
        when(chatMessageRepository.findById(1L)).thenReturn(Optional.of(testMessage));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> 
            chatService.deleteMessage(1L, "unauthorized", false)
        );
        verify(userRepository).findByUsername("unauthorized");
        verify(chatMessageRepository).findById(1L);
        verify(chatMessageRepository, never()).save(any(ChatMessage.class));
    }
} 