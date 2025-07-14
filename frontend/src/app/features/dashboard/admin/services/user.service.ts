import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { map, Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { UserResponseDTO } from '../manage-users/models/user-responseDTO.model';
import { ProviderRequest } from '../aprove-providers/models/provider-requestDTO.model';
import { text } from 'stream/consumers';
import { ApiResponse } from '../../../../shared/models/api-response.model';
import { response } from 'express';
import { MessageResponse } from '../../../../shared/models/message-response.model';
import { PageModel } from '../../../../shared/models/page-model.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private readonly userApi = `${environment.apiBaseUrl}/v1/users`;
  private readonly adminApi = `${environment.apiBaseUrl}/v1/admins`;

  constructor(private http: HttpClient) {}

  /** Get current authenticated user's profile */
  getMyProfile(): Observable<UserResponseDTO> {
    return this.http.get<UserResponseDTO>(`${this.userApi}/me`);
  }

  /** Update current authenticated user's profile */
  updateMyProfile(user: UserResponseDTO): Observable<UserResponseDTO> {
    return this.http.put<UserResponseDTO>(`${this.userApi}/me`, user);
  }

  /** Delete the currently authenticated user's account */
  deleteMyAccount(): Observable<void> {
    return this.http.delete<void>(`${this.userApi}/me`);
  }

  /** Admin: Get all users */
  getAllUsers(): Observable<UserResponseDTO[]> {
    return this.http.get<ApiResponse<PageModel<UserResponseDTO>>>(`${this.adminApi}/users`).pipe(
      map(response => response.data.content)
    );
  }

  /** Admin: Get all users except admins */
  getAllNonAdminUsers(): Observable<UserResponseDTO[]> {
    return this.http.get<UserResponseDTO[]>(`${this.adminApi}/nonAdmin`);
  }

  /** Admin: Enable a user by email */
  enableUser(email: string): Observable<string> {
    return this.http.patch<MessageResponse>(`${this.adminApi}/users/${email}/enable`, null).pipe(
      map(response => response.message)
    );
  }

  /** Admin: Disable a user by email */
  disableUser(email: string): Observable<string> {
    return this.http.patch<MessageResponse>(`${this.adminApi}/users/${email}/disable`, null).pipe(
      map(response => response.message)
    );
  }

  /** Request to become a service provider */
  requestToBecomeProvider(): Observable<string> {
    return this.http.post<MessageResponse>(`${this.userApi}/me/request-to-become-provider`, null).pipe(
      map(response => response.message)
    );
  }

  getProviderRequestStatus(): Observable<{ requested: boolean; approved: boolean }> {
    return this.http.get<ApiResponse<{ requested: boolean; approved: boolean }>>(
      `${this.userApi}/me/provider-request/status`
    ).pipe(
      map(response => response.data)
    );
  }

  /** Admin: Approve a provider request */
  approveProviderRequest(requestId: number): Observable<string> {
    return this.http.patch<MessageResponse>(`${this.adminApi}/provider-requests/${requestId}/approve`, null).pipe(
      map(response => response.message)
    );
  }

  /** Admin: Get all unapproved provider requests */
  getUnapprovedProviderRequests(): Observable<ProviderRequest[]> {
    return this.http.get<ApiResponse<PageModel<ProviderRequest>>>(`${this.adminApi}/provider-requests/pending`).pipe(
      map(response => response.data.content)
    );
  }
}
