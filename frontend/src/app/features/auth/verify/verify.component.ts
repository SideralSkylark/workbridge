import { Component, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { VerifyRequest } from './model/verify-request.model';
import { Subscription, interval } from 'rxjs';

@Component({
  selector: 'app-verify',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './verify.component.html',
  styleUrls: ['./verify.component.scss']
})
export class VerifyComponent implements OnDestroy {
  verifyForm!: FormGroup;
  submitted = false;
  loading = false;
  error = '';
  email: string = '';
  resendCooldown = 0; // Initialize cooldown to 0 (no cooldown)
  resendInProgress = false;
  private cooldownSubscription?: Subscription;
  private readonly COOLDOWN_DURATION = 30; // 30 seconds cooldown

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

    // Check if there's an existing cooldown in localStorage
    const cooldownExpiry = localStorage.getItem('resend_cooldown_expiry');
    if (cooldownExpiry) {
      const remainingSeconds = Math.max(0, Math.floor((new Date(cooldownExpiry).getTime() - Date.now()) / 1000));
      if (remainingSeconds > 0) {
        this.startCooldown(remainingSeconds);
      }
    }
  }

  get f() {
    return this.verifyForm.controls;
  }

  onSubmit() {
    this.submitted = true;
    this.error = '';

    if (this.verifyForm.invalid) return;

    this.loading = true;
    const request: VerifyRequest = {
      email: this.email,
      code: this.verifyForm.value.code
    }

    this.authService.verify(request).subscribe({
      next: () => {
        localStorage.removeItem('verification_email');
        localStorage.removeItem('resend_cooldown_expiry'); // Clear cooldown on successful verification
        this.router.navigate(['/login']);
      },
      error: () => {
        this.error = 'Invalid code or expired. Please try again.';
        this.loading = false;
      }
    });
  }

  resendCode() {
    if (this.resendCooldown > 0) return;

    this.resendInProgress = true;

    this.authService.resendVerification(this.email).subscribe({
      next: () => {
        this.resendInProgress = false;
        this.startCooldown(this.COOLDOWN_DURATION);
        // You might want to replace this with a more elegant notification
        this.error = ''; // Clear any previous errors
      },
      error: (err) => {
        this.resendInProgress = false;
        this.error = 'Failed to resend verification code. Please try again.';
        console.error('Resend error:', err);
      }
    });
  }

  private startCooldown(seconds: number) {
    this.resendCooldown = seconds;

    // Store cooldown expiry in localStorage
    const expiryDate = new Date(Date.now() + seconds * 1000);
    localStorage.setItem('resend_cooldown_expiry', expiryDate.toISOString());

    // Start countdown timer
    this.cooldownSubscription = interval(1000).subscribe(() => {
      this.resendCooldown--;

      if (this.resendCooldown <= 0) {
        this.cooldownSubscription?.unsubscribe();
        localStorage.removeItem('resend_cooldown_expiry');
      }
    });
  }

  ngOnDestroy() {
    this.cooldownSubscription?.unsubscribe();
  }
}
