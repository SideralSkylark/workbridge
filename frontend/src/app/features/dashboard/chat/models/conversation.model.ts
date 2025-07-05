interface Conversation {
    id: string;
    user: ChatUser;
    messages: ChatMessage[];
    lastMessage: string;
    lastMessageTime: Date;
    unreadCount: number;
}