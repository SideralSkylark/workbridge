  import { Component } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
  import { Router, RouterModule } from '@angular/router';
  import { AuthService } from '../auth.service';
  import { console } from 'inspector';
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
      this.loginForm = this.formBuilder.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(8)]],
        rememberMe: [false]
      });
    }

    get f() { return this.loginForm.controls; }

    onSubmit() {
      this.submitted = true;
      this.error = '';

      if (this.loginForm.invalid) return;

      this.loading = true;

      const loginRequest: LoginRequest = {
        email: this.loginForm.value.email,
        password: this.loginForm.value.password
      };


      this.authService.login(loginRequest).subscribe({
        next: () => {
          this.loading = false;

          const role = this.authService.getCurrentUserRole();

          switch (role) {
            case 'ADMIN':
              this.router.navigate(['dashboard', 'admin']);
              break;
            case 'SERVICE_SEEKER':
              this.router.navigate(['dashboard', 'requests']);
              break;
            case 'SERVICE_PROVIDER':
              this.router.navigate(['dashboard', 'manage']);
              break;
            default:
              this.router.navigate(['dashboard']);
          }
        },
        error: (err) => {
          this.error = 'Invalid credentials or server error';
          this.loading = false;
        }
      });
    }

    quickLogin(role: 'admin' | 'seeker' | 'provider') {
      const credentials = {
        admin:    { email: 'sidik@gmail.com',    password: 'sidik123' },
        seeker:   { email: 'seeker@gmail.com',   password: 'seeker123' },
        provider: { email: 'provider@gmail.com', password: 'provider123' }
      };

      const selected = credentials[role];

      this.loginForm.patchValue({
        email: selected.email,
        password: selected.password
      });

      this.onSubmit();
    }

    togglePasswordVisibility() {
    this.showPassword = !this.showPassword;

    const passwordField = document.querySelector('#password') as HTMLInputElement;
    if (passwordField) {
      passwordField.type = this.showPassword ? 'text' : 'password';
    }
  }

}
