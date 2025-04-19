import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';
import SockJS from 'sockjs-client'; 
import * as Stomp from 'stompjs'; 
import { AuthService } from '../auth/auth.service';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class ChatService {
  private stompClient: any;
  private messageSubject = new Subject<any>();

  constructor(private authService: AuthService, private http: HttpClient) {}

 
  connect(): void {
    const token = this.authService.getToken();
    if (!token) {
      console.error('Token JWT não encontrado.');
      return;
    }

    const currentUser = this.decodeJwtUsername(token);
    if (!currentUser) {
      console.error('Não foi possível extrair o username do token.');
      return;
    }


    const socket = new SockJS(`${environment.apiBaseUrl}/v1/ws-chat?token=${token}`);

    
    this.stompClient = Stomp.over(socket);


   
    this.stompClient.connect({}, (frame: string) => {

    
      this.stompClient.subscribe(`/topic/users/${currentUser}`, (message: any) => {
        const msg = JSON.parse(message.body);
        this.messageSubject.next(msg);
      });

    }, (error: string) => {
      console.error('Erro na conexão STOMP:', error);
    });
  }


  sendMessage(payload: { recipientUsername: string; content: string; timestamp: string }): void {
    const currentUser = this.decodeJwtUsername(this.authService.getToken()!);
    if (!this.stompClient || !this.stompClient.connected) {
      console.error('STOMP client não está conectado.');
      return;
    }

    const messagePayload = {
      senderUsername: currentUser,
      ...payload
    };

 
    this.stompClient.send('/app/chat', {}, JSON.stringify(messagePayload));
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

  private connected = false;

ensureConnection() {
  if (!this.connected) {
    this.connect();
  }
}

  
}