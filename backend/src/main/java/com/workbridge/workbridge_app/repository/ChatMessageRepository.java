package com.workbridge.workbridge_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.workbridge.workbridge_app.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findByRecipientUsername(String username);

    List<ChatMessage> findBySenderUsername(String username);

    List<ChatMessage> findBySenderUsernameOrRecipientUsernameOrderByTimestampAsc(String sender, String recipient);

    // Mensagens visíveis entre dois usuários
    @Query("SELECT m FROM ChatMessage m WHERE "
            + "((m.senderUsername = :user1 AND m.recipientUsername = :user2 AND m.deletedBySender = false) "
            + "OR (m.senderUsername = :user2 AND m.recipientUsername = :user1 AND m.deletedByRecipient = false)) "
            + "ORDER BY m.timestamp ASC")
    List<ChatMessage> findVisibleMessagesBetween(String user1, String user2);

    // Últimas mensagens por conversa (opcional para lista de chats)
    @Query("SELECT m FROM ChatMessage m WHERE " + "((m.senderUsername = :username AND m.deletedBySender = false) "
            + "OR (m.recipientUsername = :username AND m.deletedByRecipient = false))")
    List<ChatMessage> findVisibleMessagesForUser(String username);

}
