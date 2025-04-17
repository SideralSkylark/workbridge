package com.workbridge.workbridge_app.repository;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.workbridge.workbridge_app.entity.ChatMessage;
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRecipientUsername(String username);
    List<ChatMessage> findBySenderUsername(String username);
}
