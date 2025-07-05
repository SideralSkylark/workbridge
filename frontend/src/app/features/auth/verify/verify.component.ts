// verify.component.ts
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.scss']
})
export class VerifyComponent {
  verifyForm!: FormGroup;
  submitted = false;
  loading = false;
  error = '';
  email: string = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    const emailFromStorage = localStorage.getItem('verification_email');
    if (!emailFromStorage) {
      this.router.navigate(['/register']);
      return;
    }

    this.email = emailFromStorage;

    this.verifyForm = this.fb.group({
      code: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  get f() {
    return this.verifyForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';

    if (this.verifyForm.invalid) return;

    this.loading = true;
    const code = this.verifyForm.value.code;

    this.authService.verifyCode(this.email, code).subscribe({
      next: () => {
        localStorage.removeItem('verification_email');
        this.router.navigate(['/login']);
      },
      error: () => {
        this.error = 'Invalid code or expired. Please try again.';
        this.loading = false;
      }
    });
  }

  resendCode() {
    this.authService.resendVerification(this.email).subscribe({
      next: () => alert('A new code was sent to your email.'),
      error: () => alert('Failed to resend verification code.')
    });
  }
}