import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client';
import { Client, IMessage } from '@stomp/stompjs';
import { AuthService } from '../../../auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient!: Client;
  private messageSubject = new Subject<any>();
  private connected = false;

  constructor(private authService: AuthService, private http: HttpClient) {}

  connect(): void {
    const token = ''; /*this.authService.getToken();*/
    if (!token) {
      console.error('Token JWT não encontrado.');
      return;
    }

    const currentUser = this.decodeJwtUsername(token);
    if (!currentUser) {
      console.error('Não foi possível extrair o username do token.');
      return;
    }

    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(`${environment.apiBaseUrl}/v1/ws-chat?token=${token}`),
      debug: (str) => console.log(str), // Pode remover em produção
      reconnectDelay: 5000
    });

    this.stompClient.onConnect = () => {
      this.connected = true;

      this.stompClient.subscribe(`/topic/users/${currentUser}`, (message: IMessage) => {
        const msg = JSON.parse(message.body);
        this.messageSubject.next(msg);
      });
    };

    this.stompClient.onStompError = (frame) => {
      console.error('Erro STOMP:', frame.headers['message'], frame.body);
    };

    this.stompClient.activate();
  }

  sendMessage(payload: { recipientUsername: string; content: string; timestamp: string }): void {
    const token = '';/*this.authService.getToken();*/
    const currentUser = this.decodeJwtUsername(token!);

    if (!this.stompClient || !this.connected) {
      console.error('STOMP client não está conectado.');
      return;
    }

    const messagePayload = {
      senderUsername: currentUser,
      ...payload
    };

    this.stompClient.publish({
      destination: '/app/chat',
      body: JSON.stringify(messagePayload)
    });
  }

  onMessage(): Observable<any> {
    return this.messageSubject.asObservable();
  }

  private decodeJwtUsername(token: string): string | null {
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = atob(payloadBase64);
      return JSON.parse(decodedPayload).sub;
    } catch (e) {
      console.error('Erro ao decodificar token JWT:', e);
      return null;
    }
  }

  loadMessagesForUser(username: string): Observable<any[]> {
    return this.http.get<any[]>(`${environment.apiBaseUrl}/v1/chat/${username}`);
  }

  deleteMessage(messageId: number, currentUsername: string, deleteForAll: boolean) {
    return this.http.post(
      `${environment.apiBaseUrl}/v1/chat/message/${messageId}/delete?currentUsername=${currentUsername}&deleteForAll=${deleteForAll}`,
      {}
    );
  }

  deleteConversation(currentUsername: string, otherUsername: string) {
    return this.http.delete(
      `${environment.apiBaseUrl}/v1/chat/conversation/${otherUsername}?currentUsername=${currentUsername}`
    );
  }

  ensureConnection() {
    if (!this.connected) {
      this.connect();
    }
  }
}
