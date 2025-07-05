import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../../environments/environment';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { RegisterRequestDTO } from './register/models/register-requestDTO.model';
import { RegisterResponseDTO } from './register/models/register-responseDTO.model';

interface AuthResponse {
  token: string;
  id: number;
  username: string;
  email: string;
  roles: string[];
  updatedAt?: string;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient, private router: Router) {}

  register(data: RegisterRequestDTO): Observable<RegisterResponseDTO> {
    return this.http.post<RegisterResponseDTO>(`${this.apiUrl}/v1/auth/register`, data);
  }

  verify(email: string, code: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}/v1/auth/verify`, { email, code });
  }

  resendVerification(email: string): Observable<RegisterResponseDTO> {
    return this.http.post<RegisterResponseDTO>(`${this.apiUrl}/v1/auth/resend-verification`, { email });
  }

  verifyCode(email: string, code: string) {
    return this.verify(email, code);
  }

  login(email: string, password: string): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/v1/auth/login`, { email, password }).pipe(
      tap(response => {
        localStorage.setItem('jwt', response.token);
        localStorage.setItem('user', JSON.stringify({
          id: response.id,
          username: response.username,
          email: response.email,
          roles: response.roles
        }));
      })
    );
  }

  logout() {
    localStorage.removeItem('jwt');
    localStorage.removeItem('user')
    this.router.navigate(['/login']);
  }

  isLoggedIn(): boolean {
    return !!localStorage.getItem('jwt');
  }

  getToken(): string | null {
    return localStorage.getItem('jwt');
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
