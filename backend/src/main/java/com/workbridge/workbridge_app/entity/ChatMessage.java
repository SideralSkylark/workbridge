package com.workbridge.workbridge_app.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderUsername;
    private String recipientUsername;
    private String content;
    private String timestamp;

    @Column(columnDefinition = "boolean default false")
    private boolean deletedBySender;

    @Column(columnDefinition = "boolean default false")
    private boolean deletedByRecipient;

}
