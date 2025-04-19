package com.workbridge.workbridge_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.workbridge.workbridge_app.entity.ChatMessage;
import com.workbridge.workbridge_app.entity.ApplicationUser;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByRecipient(ApplicationUser recipient);

    List<ChatMessage> findBySender(ApplicationUser sender);

    List<ChatMessage> findBySenderOrRecipientOrderByTimestampAsc(ApplicationUser sender, ApplicationUser recipient);

    @Query("SELECT m FROM ChatMessage m WHERE "
         + "((m.sender = :user1 AND m.recipient = :user2 AND m.deletedBySender = false) "
         + "OR (m.sender = :user2 AND m.recipient = :user1 AND m.deletedByRecipient = false)) "
         + "ORDER BY m.timestamp ASC")
    List<ChatMessage> findVisibleMessagesBetween(ApplicationUser user1, ApplicationUser user2);

    @Query("SELECT m FROM ChatMessage m WHERE "
         + "((m.sender = :user AND m.deletedBySender = false) "
         + "OR (m.recipient = :user AND m.deletedByRecipient = false))")
    List<ChatMessage> findVisibleMessagesForUser(ApplicationUser user);
}
