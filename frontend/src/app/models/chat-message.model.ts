interface ChatMessage {
    id?: number;
    content: string;
    senderUsername: string;
    recipientUsername: string;
    timestamp: Date;
    deletedBySender?: boolean;
    deletedByRecipient?: boolean;
  }