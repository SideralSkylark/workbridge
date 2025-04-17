// import { Component, OnInit } from '@angular/core';
// import { CommonModule } from '@angular/common';
// import { FormsModule } from '@angular/forms';
// import { TruncatePipe } from '../../shared/pipes/truncate.pipe';
// // import { ChatService } from '../../services/chat.service';

// interface ChatUser {
//   id: string;
//   name: string;
//   avatar?: string;
// }

// interface ChatMessage {
//   content: string;
//   senderId: string;
//   recipientId: string;
//   timestamp: Date;
// }

// interface Conversation {
//   id: string; // normalmente o ID do outro usuário
//   user: ChatUser;
//   messages: ChatMessage[];
//   lastMessage: string;
//   lastMessageTime: Date;
//   unreadCount: number;
// }

// @Component({
//   selector: 'app-chat',
//   standalone: true,
//   imports: [CommonModule, FormsModule, TruncatePipe],
//   templateUrl: './chat.component.html',
//   styleUrls: ['./chat.component.scss']
// })
// export class ChatComponent implements OnInit {
//   conversations: Conversation[] = [];
//   activeChatId: string | null = null;
//   newMessage = '';
//   currentUserId: string = '';

//   // constructor(private chatService: ChatService) {}

//   get activeConversation(): Conversation | undefined {
//     return this.conversations.find(c => c.id === this.activeChatId);
//   }

//   ngOnInit(): void {
//     this.chatService.connect();

//     this.currentUserId = this.chatService['decodeJwtUsername'](
//       localStorage.getItem('jwt') || ''
//     ) || '';

//     this.chatService.onMessage().subscribe((msg: ChatMessage) => {
//       this.handleIncomingMessage(msg);
//     });
//   }

//   handleIncomingMessage(msg: ChatMessage) {
//     const partnerId = msg.senderId === this.currentUserId ? msg.recipientId : msg.senderId;
//     let conversation = this.conversations.find(c => c.id === partnerId);

//     if (!conversation) {
//       conversation = {
//         id: partnerId,
//         user: {
//           id: partnerId,
//           name: partnerId, // você pode substituir pelo nome real se tiver um endpoint
//         },
//         messages: [],
//         lastMessage: '',
//         lastMessageTime: new Date(),
//         unreadCount: 0
//       };
//       this.conversations.push(conversation);
//     }

//     conversation.messages.push(msg);
//     conversation.lastMessage = msg.content;
//     conversation.lastMessageTime = new Date(msg.timestamp);

//     if (conversation.id !== this.activeChatId) {
//       conversation.unreadCount += 1;
//     }
//   }

//   selectChat(chatId: string): void {
//     this.activeChatId = chatId;
//     const conv = this.conversations.find(c => c.id === chatId);
//     if (conv) conv.unreadCount = 0;
//   }

//   sendMessage(): void {
//     if (!this.newMessage.trim() || !this.activeChatId) return;

//     const msg: ChatMessage = {
//       content: this.newMessage,
//       senderId: this.currentUserId,
//       recipientId: this.activeChatId,
//       timestamp: new Date()
//     };

//     this.chatService.sendMessage({
//       recipientId: msg.recipientId,
//       content: msg.content,
//       timestamp: msg.timestamp.toISOString()
//     });

//     this.handleIncomingMessage(msg);
//     this.newMessage = '';
//   }

//   startNewChat(): void {
//     console.log('Iniciar nova conversa...');
//     // lógica futura para buscar contatos, etc
//   }
// }
