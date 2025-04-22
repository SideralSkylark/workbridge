package com.workbridge.workbridge_app.controller;


import com.workbridge.workbridge_app.dto.ChatMessageRequestDTO;
import com.workbridge.workbridge_app.dto.ChatMessageResponseDTO;
import com.workbridge.workbridge_app.exception.UserNotFoundException;
import com.workbridge.workbridge_app.service.ChatService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @MessageMapping("/chat")
    public void sendMessage(ChatMessageRequestDTO message) {
        chatService.sendPrivateMessage(message);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @GetMapping("/api/v1/chat/{username}")
    @ResponseBody
    public List<ChatMessageResponseDTO> getMessagesForUser(@PathVariable String username) {
        return chatService.getMessages(username);
    }

    @DeleteMapping("/api/v1/chat/conversation/{otherUsername}")
    public ResponseEntity<?> deleteConversation(@RequestParam String currentUsername,
            @PathVariable String otherUsername) {
        chatService.deleteConversationForUser(currentUsername, otherUsername);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api/v1/chat/message/{id}/delete")
    public ResponseEntity<?> deleteMessage(@PathVariable Long id, @RequestParam String currentUsername,
            @RequestParam boolean deleteForAll) {
        chatService.deleteMessage(id, currentUsername, deleteForAll);
        return ResponseEntity.ok().build();
    }
}
