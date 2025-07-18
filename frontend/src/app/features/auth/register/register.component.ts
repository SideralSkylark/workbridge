import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormControl,
  FormGroup,
  ReactiveFormsModule,
  Validators,
  ValidationErrors
} from '@angular/forms';
import { Router, RouterModule } from '@angular/router';
import { AuthService } from '../auth.service';
import { RegisterRequestDTO } from './models/register-requestDTO.model';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  registerForm: FormGroup<{
    username: FormControl<string>;
    email: FormControl<string>;
    password: FormControl<string>;
    confirmPassword: FormControl<string>;
    roles: FormControl<string[]>;
    status: FormControl<string>;
  }>;

  submitted = false;
  loading = false;
  error = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {
    this.registerForm = this.fb.group(
      {
        username: this.fb.control('', Validators.required),
        email: this.fb.control('', [Validators.required, Validators.email]),
        password: this.fb.control('', [Validators.required, Validators.minLength(6)]),
        confirmPassword: this.fb.control('', Validators.required),
        roles: this.fb.control(['SERVICE_SEEKER']),
        status: this.fb.control('INACTIVE')
      },
      { validators: this.passwordMatchValidator }
    );
  }

  get f() {
    return this.registerForm.controls;
  }

  passwordMatchValidator(form: FormGroup): ValidationErrors | null {
    const password = form.get('password')?.value;
    const confirm = form.get('confirmPassword')?.value;
    return password && confirm && password !== confirm ? { mismatch: true } : null;
  }

  onSubmit(): void {
    this.submitted = true;
    this.error = '';

    if (this.registerForm.invalid) return;

    this.loading = true;

    const request: RegisterRequestDTO = {
      username: this.f.username.value!,
      email: this.f.email.value!,
      password: this.f.password.value!,
      roles: this.f.roles.value!,
      status: this.f.status.value!
    };

    this.authService.register(request).subscribe({
      next: () => {
        localStorage.setItem('verification_email', request.email);
        this.router.navigate(['/verify']);
      },
      error: (err) => {
        this.error = 'Registration failed. Email may already exist.';
        console.error('Registration error:', err);
        this.loading = false;
      }
    });
  }

  getPasswordStrength(): number {
  const password = this.registerForm.get('password')?.value || '';

  if (password.length === 0) return 0;

  let strength = 0;

  // Length check
  if (password.length >= 6) strength += 25;
  if (password.length >= 8) strength += 25;

  // Character variety checks
  if (/[a-z]/.test(password)) strength += 15;
  if (/[A-Z]/.test(password)) strength += 15;
  if (/[0-9]/.test(password)) strength += 10;
  if (/[^A-Za-z0-9]/.test(password)) strength += 10;

  return Math.min(strength, 100);
}

getPasswordStrengthColor(): string {
  const strength = this.getPasswordStrength();

  if (strength < 30) return '#ce7067'; // Red
  if (strength < 60) return '#ee7c1fff'; // Orange
  if (strength < 80) return '#dfa11a'; // Yellow
  return '#3e8343'; // Green
}

getPasswordStrengthText(): string {
  const strength = this.getPasswordStrength();

  if (strength < 30) return 'Weak';
  if (strength < 60) return 'Fair';
  if (strength < 80) return 'Good';
  return 'Strong';
}
}
