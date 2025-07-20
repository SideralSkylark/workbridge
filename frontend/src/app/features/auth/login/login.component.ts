import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { LoginRequest } from './model/login-request.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  loginForm: FormGroup;
  loading = false;
  submitted = false;
  error = '';
  showPassword = false;

  constructor(
    private formBuilder: FormBuilder,
    private router: Router,
    private authService: AuthService
  ) {
    this.loginForm = this.createLoginForm();
  }

  private createLoginForm(): FormGroup {
    return this.formBuilder.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required, Validators.minLength(8)]],
      rememberMe: [false]
    });
  }

  get f() {
    return this.loginForm.controls;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.loginForm.invalid) return;

    this.loading = true;

    const loginRequest: LoginRequest = {
      email: this.loginForm.get('email')?.value,
      password: this.loginForm.get('password')?.value
    };

    this.authService.login(loginRequest).subscribe({
      next: () => {
        this.loading = false;
        this.navigateByRole();
      },
      error: () => {
        this.error = 'Invalid credentials or server error';
        this.loading = false;
      }
    });
  }

  private navigateByRole(): void {
    const role = this.authService.getCurrentUserRole();

    const routes: Record<string, string[]> = {
      ADMIN: ['dashboard', 'admin'],
      SERVICE_SEEKER: ['dashboard', 'requests'],
      SERVICE_PROVIDER: ['dashboard', 'manage']
    };

    this.router.navigate(routes[role ?? ''] ?? ['dashboard']);
  }

  quickLogin(role: 'admin' | 'seeker' | 'provider'): void {
    const credentials = {
      admin: { email: 'sidik@gmail.com', password: 'sidik123' },
      seeker: { email: 'seeker@gmail.com', password: 'seeker123' },
      provider: { email: 'provider@gmail.com', password: 'provider123' }
    }[role];

    this.loginForm.patchValue(credentials);
    this.onSubmit();
  }

  togglePasswordVisibility(): void {
    this.showPassword = !this.showPassword;
  }
}
