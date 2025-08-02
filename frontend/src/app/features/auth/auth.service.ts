import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { tap, map } from 'rxjs/operators';
import { RegisterRequestDTO } from './register/models/register-requestDTO.model';
import { RegisterResponseDTO } from './register/models/register-responseDTO.model';
import { ApiResponse } from '../../shared/models/api-response.model';
import { LoginRequest } from './login/model/login-request.model';
import { AuthResponse } from './model/auth-response.model';
import { response } from 'express';
import { VerifyRequest } from './verify/model/verify-request.model';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient, private router: Router) {}

  register(request: RegisterRequestDTO): Observable<RegisterResponseDTO> {
    return this.http.post<ApiResponse<RegisterResponseDTO>>(
      `${this.apiUrl}/v1/auth/register`, request
    ).pipe(
      map(response => response.data)
    );
  }

  verify(request: VerifyRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${this.apiUrl}/v1/auth/verify`, request
    ).pipe(
      tap(response => this.storeUserInfo(response.data)),
      map(response => response.data)
    );
  }

  resendVerification(email: string): Observable<RegisterResponseDTO> {
    return this.http.post<ApiResponse<RegisterResponseDTO>>(
      `${this.apiUrl}/v1/auth/resend-verification/${email}`, null
    ).pipe(
      map(response => response.data)
    );
  }

  login(request: LoginRequest): Observable<AuthResponse> {
    return this.http.post<ApiResponse<AuthResponse>>(
      `${this.apiUrl}/v1/auth/login`, request, { withCredentials: true }
      ).pipe(
      tap(response => this.storeUserInfo(response.data)),
      map(response => response.data)
    );
  }

  logout(): void {
    this.http.post(`${this.apiUrl}/v1/auth/logout`, {}, { withCredentials: true })
      .subscribe({
        next: () => {
          this.clearUserInfo();
          this.router.navigate(['/login']);
        },
        error: err => {
          console.error('Logout failed', err);
          this.clearUserInfo();
          this.router.navigate(['/login']);
        }
      })
  }

  private storeUserInfo(user: AuthResponse): void {
    localStorage.setItem("user", JSON.stringify({
      id: user.id,
      username: user.username,
      email: user.email,
      roles: user.roles
    }));
  }

  private clearUserInfo(): void {
    localStorage.removeItem('user');
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('user');
  }

  getUserRoles(): string[] {
    const user = localStorage.getItem('user');
    return user ? JSON.parse(user).roles : [];
  }

  getCurrentUserRole(): string | null {
    const user = JSON.parse(localStorage.getItem('user') || '{}');
    return user?.roles?.[0] ?? null;
  }

  getUserId(): number | null {
    const userJson = localStorage.getItem('user');
    if (!userJson) return null;

    try {
      const user = JSON.parse(userJson);
      return user?.id ?? null;
    } catch (e) {
      console.error('Failed to parse user from localStorage', e);
      return null;
    }
  }

  hasRole(role: string): boolean {
    return this.getUserRoles().includes(role);
  }
}
