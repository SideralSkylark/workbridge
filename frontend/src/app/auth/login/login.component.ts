  import { Component } from '@angular/core';
  import { CommonModule } from '@angular/common';
  import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
  import { Router, RouterModule } from '@angular/router';
  import { AuthService } from '../auth.service';

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

    constructor(
      private formBuilder: FormBuilder,
      private router: Router,
      private authService: AuthService
    ) {
      this.loginForm = this.formBuilder.group({
        email: ['', [Validators.required, Validators.email]],
        password: ['', [Validators.required, Validators.minLength(6)]],
        rememberMe: [false]
      });
    }

    get f() { return this.loginForm.controls; }

    onSubmit() {
      this.submitted = true;
      this.error = '';
    
      if (this.loginForm.invalid) return;
    
      this.loading = true;
    
      const { email, password } = this.loginForm.value;
    
      this.authService.login(email, password).subscribe({
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
        seeker:   { email: 'sidik@gmail.com',   password: 'sidik123' },
        provider: { email: 'provider@gmail.com', password: 'provider123' }
      };
    
      const selected = credentials[role];
    
      this.loginForm.patchValue({
        email: selected.email,
        password: selected.password
      });
    
      this.onSubmit(); 
    }    
  }