import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from './auth.service';  // Make sure to import your AuthService

@Injectable({
  providedIn: 'root'
})
export class AuthGuard implements CanActivate {

  constructor(private authService: AuthService, private router: Router) {}

  canActivate(
    next: ActivatedRouteSnapshot,
    state: RouterStateSnapshot
  ): boolean {
    // Check if the user is authenticated, you can check for a token or some flag in the AuthService
    if (this.authService.isLoggedIn()) {
      return true;
    } else {
      // If not authenticated, redirect to login
      this.router.navigate(['/login']);
      return false;
    }
  }
}