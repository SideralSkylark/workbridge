import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = environment.apiBaseUrl;

  constructor(private http: HttpClient, private router: Router) {}

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