import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../environments/environment';
import { UserResponseDTO } from '../manage-users/models/user-responseDTO.model';
import { ProviderRequest } from '../aprove-providers/models/provider-requestDTO.model';
import { text } from 'stream/consumers';

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
    return this.http.get<UserResponseDTO[]>(`${this.adminApi}`);
  }

  /** Admin: Get all users except admins */
  getAllNonAdminUsers(): Observable<UserResponseDTO[]> {
    return this.http.get<UserResponseDTO[]>(`${this.adminApi}/nonAdmin`);
  }

  /** Admin: Enable a user by email */
  enableUser(email: string): Observable<string> {
    const params = new HttpParams().set('email', email);
    return this.http.put(`${this.adminApi}/enable`, null, { params, responseType: 'text' });
  }

  /** Admin: Disable a user by email */
  disableUser(email: string): Observable<string> {
    const params = new HttpParams().set('email', email);
    return this.http.put(`${this.adminApi}/disable`, null, { params, responseType: 'text' });
  }

  /** Request to become a service provider */
  requestToBecomeProvider(): Observable<string> {
    return this.http.post(`${this.userApi}/me/request-to-become-provider`, {}, {responseType: 'text'});
  }

  getProviderRequestStatus(): Observable<{ requested: boolean; approved: boolean }> {
    return this.http.get<{ requested: boolean; approved: boolean }>(
      `${this.userApi}/me/provider-request-status`
    );
  }

  /** Admin: Approve a provider request */
  approveProviderRequest(requestId: number): Observable<string> {
    return this.http.put<string>(`${this.adminApi}/approve-provider/${requestId}`, {});
  }

  /** Admin: Get all unapproved provider requests */
  getUnapprovedProviderRequests(): Observable<ProviderRequest[]> {
    return this.http.get<ProviderRequest[]>(`${this.adminApi}/provider-requests`);
  }
}
