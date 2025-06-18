package com.workbridge.workbridge_app.chat.entity;

import com.workbridge.workbridge_app.user.entity.ApplicationUser;

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

    // Relacionamento Muitos para Um com a entidade Usuario
    @ManyToOne
    @JoinColumn(name = "sender_id", referencedColumnName = "id", nullable = false)
    private ApplicationUser sender; // Relaciona-se com a tabela 'application_user'

    @ManyToOne
    @JoinColumn(name = "recipient_id", referencedColumnName = "id", nullable = false)
    private ApplicationUser recipient; // Relaciona-se com a tabela 'application_user'

    private String content;
    private String timestamp;

    @Column(columnDefinition = "boolean default false")
    private boolean deletedBySender;

    @Column(columnDefinition = "boolean default false")
    private boolean deletedByRecipient;

}
