import { Component, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { TruncatePipe } from '../../shared/pipes/truncate.pipe';
import { ChatService } from '../../services/chat.service';
import { ActivatedRoute } from '@angular/router';

interface ChatUser {
  id: string;
  name: string;
  avatar?: string;
}

interface ChatMessage {
  id?: number;
  content: string;
  senderUsername: string;
  recipientUsername: string;
  timestamp: Date;
  deletedBySender?: boolean;
  deletedByRecipient?: boolean;
}

interface Conversation {
  id: string; // normalmente o ID do outro usuário
  user: ChatUser;
  messages: ChatMessage[];
  lastMessage: string;
  lastMessageTime: Date;
  unreadCount: number;
}

@Component({
  selector: 'app-chat',
  standalone: true,
  imports: [CommonModule, FormsModule, TruncatePipe],
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.scss'],
})
export class ChatComponent implements OnInit {
  conversations: Conversation[] = [];
  activeChatId: string | null = null;
  newMessage = '';
  currentUserId: string = '';

  constructor(
    private chatService: ChatService,
    private route: ActivatedRoute
  ) {}

  get activeConversation(): Conversation | undefined {
    return this.conversations.find((c) => c.id === this.activeChatId);
  }
  private sortConversations(): void {
    this.conversations.sort(
      (a, b) => b.lastMessageTime.getTime() - a.lastMessageTime.getTime()
    );
  }

  ngOnInit(): void {
    const userStr = localStorage.getItem('user');
    if (userStr) {
      const user = JSON.parse(userStr);
      this.currentUserId = user.username;
    }
  
    // 1️⃣ Primeiro carregar mensagens
    this.chatService.loadMessagesForUser(this.currentUserId).subscribe((messages) => {
      const grouped = new Map<string, ChatMessage[]>();
  
      messages.forEach((msg) => {
        if (
          (msg.senderUsername === this.currentUserId && msg.deletedBySender) ||
          (msg.recipientUsername === this.currentUserId && msg.deletedByRecipient)
        ) {
          return;
        }
  
        const partner =
          msg.senderUsername === this.currentUserId
            ? msg.recipientUsername
            : msg.senderUsername;
  
        if (!grouped.has(partner)) {
          grouped.set(partner, []);
        }
        grouped.get(partner)!.push(msg);
      });
  
      grouped.forEach((msgs, partnerId) => {
        const conversation: Conversation = {
          id: partnerId,
          user: { id: partnerId, name: partnerId },
          messages: msgs,
          lastMessage: msgs[msgs.length - 1]?.content ?? '',
          lastMessageTime: new Date(msgs[msgs.length - 1]?.timestamp ?? Date.now()),
          unreadCount: 0,
        };
        this.conversations.push(conversation);
      });
  
      this.sortConversations();
  
      // 2️⃣ Só depois que tudo foi carregado, processa queryParams
      this.route.queryParams.subscribe((params) => {
        const recipientUsername = params['recipient'];
        if (recipientUsername) {
          const alreadyExists = this.conversations.some(
            (c) => c.id === recipientUsername
          );
  
          if (!alreadyExists) {
            this.createChatIfNeeded(recipientUsername); // cria se não veio nada do backend
          }
  
          this.selectChat(recipientUsername); // ativa o chat
        }
      });
    });
  
    // 3️⃣ WebSocket
    this.chatService.onMessage().subscribe((msg: ChatMessage) => {
      console.log('[FRONT] Mensagem recebida por WebSocket:', msg);
      this.handleIncomingMessage(msg);
    });
  }
  

  createChatIfNeeded(recipientUsername: string) {
    const alreadyExists = this.conversations.some(
      (c) => c.id === recipientUsername
    );
    if (alreadyExists) return;
  
    this.conversations.push({
      id: recipientUsername,
      user: { id: recipientUsername, name: recipientUsername },
      messages: [],
      lastMessage: '',
      lastMessageTime: new Date(),
      unreadCount: 0,
    });
  }
  
  handleIncomingMessage(msg: ChatMessage): void {
    const partnerId =
      msg.senderUsername === this.currentUserId
        ? msg.recipientUsername
        : msg.senderUsername;
    let conversation = this.conversations.find((c) => c.id === partnerId);

    if (!conversation) {
      conversation = {
        id: partnerId,
        user: { id: partnerId, name: partnerId },
        messages: [],
        lastMessage: '',
        lastMessageTime: new Date(),
        unreadCount: 0,
      };
      this.conversations.push(conversation);
    }

    // 🔍 Verifica se a mensagem já existe
    const alreadyExists = conversation.messages.some(
      (existing) =>
        existing.content === msg.content &&
        existing.timestamp.toString() === new Date(msg.timestamp).toString() &&
        existing.senderUsername === msg.senderUsername
    );

    if (alreadyExists) {
      console.log('[chat] Ignorando mensagem duplicada');
      return;
    }

    conversation.messages.push(msg);
    conversation.lastMessage = msg.content;
    conversation.lastMessageTime = new Date(msg.timestamp);

    if (conversation.id !== this.activeChatId) {
      conversation.unreadCount += 1;
    }

    // 🆕 Mover conversa para o topo
    this.conversations = [
      conversation,
      ...this.conversations.filter((c) => c.id !== conversation!.id),
    ];

    if (conversation.id !== this.activeChatId) {
      conversation.unreadCount += 1;
    }
    this.sortConversations();
  }

  selectChat(chatId: string): void {
    // Selecionar uma conversa e zerar as mensagens não lidas
    this.activeChatId = chatId;
    const conv = this.conversations.find((c) => c.id === chatId);
    if (conv) conv.unreadCount = 0;
  }

  sendMessage(): void {
    // Não enviar se a mensagem estiver vazia ou se não houver uma conversa ativa
    if (!this.newMessage.trim() || !this.activeChatId) return;

    // Criar o objeto da mensagem
    const msg: ChatMessage = {
      content: this.newMessage,
      senderUsername: this.currentUserId,
      recipientUsername: this.activeChatId,
      timestamp: new Date(),
    };

    // Enviar a mensagem usando o serviço de chat
    this.chatService.sendMessage({
      recipientUsername: msg.recipientUsername,
      content: msg.content,
      timestamp: msg.timestamp.toISOString(),
    });

    // Adicionar a mensagem à conversa ativa
    this.handleIncomingMessage(msg);
    this.newMessage = ''; // Limpar campo de mensagem
  }

  deleteForMe(msg: ChatMessage): void {
    if (msg.id === undefined) return;
    this.chatService
      .deleteMessage(msg.id, this.currentUserId, false)
      .subscribe(() => {
        const conv = this.activeConversation;
        if (conv) {
          conv.messages = conv.messages.filter((m) => m.id !== msg.id);
        }
      });
  }

  deleteForAll(msg: ChatMessage): void {
    if (msg.id === undefined) return;
    this.chatService
      .deleteMessage(msg.id, this.currentUserId, true)
      .subscribe(() => {
        const conv = this.activeConversation;
        if (conv) {
          conv.messages = conv.messages.filter((m) => m.id !== msg.id);
        }
      });
  }

  deleteConversation(): void {
    if (!this.activeChatId) return;
    this.chatService
      .deleteConversation(this.currentUserId, this.activeChatId)
      .subscribe(() => {
        this.conversations = this.conversations.filter(
          (c) => c.id !== this.activeChatId
        );
        this.activeChatId = null;
      });
  }
  contextMenuVisible = false;
  contextMenuPosition = { x: 0, y: 0 };
  contextMessage: ChatMessage | null = null;

  onRightClick(event: MouseEvent, msg: ChatMessage) {
    event.preventDefault();
    this.contextMenuVisible = true;
    this.contextMenuPosition = { x: event.clientX, y: event.clientY };
    this.contextMessage = msg;
  }

  @HostListener('document:click')
  closeContextMenu() {
    this.contextMenuVisible = false;
  }

  startNewChat(): void {
    console.log('Iniciar nova conversa...');
    // Lógica futura para buscar contatos, etc.
  }
}
