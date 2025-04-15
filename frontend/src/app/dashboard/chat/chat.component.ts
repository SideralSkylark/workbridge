import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TruncatePipe } from '../../shared/pipes/truncate.pipe';


interface ChatUser {
  id: string;
  name: string;
  avatar: string;
  online: boolean;
}

interface ChatMessage {
  id: string;
  content: string;
  sender: 'me' | string;
  timestamp: Date;
}

interface Conversation {
  id: string;
  user: ChatUser;
  lastMessage: string;
  lastMessageTime: Date;
  unreadCount: number;
  messages: ChatMessage[];
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TruncatePipe ],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
  

})
export class ChatComponent implements OnInit {
  conversations: Conversation[] = [
    {
      id: '1',
      user: {
        id: 'provider1',
        name: 'João Eletricista',
        avatar: 'https://i.pravatar.cc/150?img=5',
        online: true
      },
      lastMessage: 'Posso ir amanhã às 14h',
      lastMessageTime: new Date(),
      unreadCount: 2,
      messages: [
        {
          id: '1',
          content: 'Olá, preciso de um eletricista',
          sender: 'me',
          timestamp: new Date(Date.now() - 3600000)
        },
        {
          id: '2',
          content: 'Bom dia! Em que posso ajudar?',
          sender: 'provider1',
          timestamp: new Date(Date.now() - 1800000)
        },
        {
          id: '3',
          content: 'Posso ir amanhã às 14h',
          sender: 'provider1',
          timestamp: new Date()
        }
      ]
    },
    // Mais conversas...
  ];

  activeChatId: string | null = null;
  newMessage = '';

  get activeConversation(): Conversation | undefined {
    return this.conversations.find(c => c.id === this.activeChatId);
  }

  ngOnInit(): void {
    // Carregar conversas do serviço
  }

  selectChat(chatId: string): void {
    this.activeChatId = chatId;
    // Marcar mensagens como lidas
    const conv = this.conversations.find(c => c.id === chatId);
    if (conv) conv.unreadCount = 0;
  }

  sendMessage(): void {
    if (!this.newMessage.trim() || !this.activeChatId) return;

    const activeConv = this.activeConversation;
    if (activeConv) {
      const newMsg: ChatMessage = {
        id: Date.now().toString(),
        content: this.newMessage,
        sender: 'me',
        timestamp: new Date()
      };

      activeConv.messages.push(newMsg);
      activeConv.lastMessage = this.newMessage;
      activeConv.lastMessageTime = new Date();
      this.newMessage = '';

      // Aqui você enviaria a mensagem para o backend
      // this.chatService.sendMessage(activeConv.id, this.newMessage);
    }
  }

  startNewChat(): void {
    // Lógica para iniciar nova conversa
    console.log('Nova conversa iniciada');
  }
}