import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../provider/services/service-form-state.service';
import { AuthService } from '../../auth/auth.service';
import { filter } from 'rxjs';
import { UserService } from '../admin/services/user.service';

@Component({
  selector: 'app-service-requests',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './seeker.component.html',
  styleUrl: './seeker.component.scss'
})
export class ServiceRequestsComponent {
  currentRoute: string = '';
  providerRequestSent: boolean = false;
  feedbackMessage: string = '';
  isProvider: boolean = false;
  showRequestCard: boolean = false;

  constructor(
    private router: Router,
    private formStateService: ServiceFormStateService,
    private authService: AuthService,
    private userService: UserService
  ) {
    this.currentRoute = this.router.url;

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
    });
    this.checkProviderStatus();
  }

  checkProviderStatus(): void {
    this.userService.getProviderRequestStatus().subscribe({
      next: (status) => {
        this.providerRequestSent = status.requested;
        this.isProvider = status.approved;
      },
      error: (err) => {
        console.error("Error checking provider status", err);
      }
    });
  }

  viewFeed(): void {
    this.router.navigate(['dashboard', 'requests', 'feed']);
  }

  viewBookings(): void {
    this.router.navigate(['dashboard', 'requests', 'bookings']);
  }

  isServiceProvider(): boolean {
    //TODO: check if the user already sent a request to become a provder
    return this.providerRequestSent;
  }

  //TODO: implement logic to also check if the user already requested to be a provider

  requestToBecomeProvider(): void {
    this.userService.requestToBecomeProvider().subscribe({
      next: () => {
        this.providerRequestSent = true;
        this.showRequestCard  = true;
        this.feedbackMessage = "Your request to become a provider has been submitted.";

        this.checkProviderStatus();
        setTimeout(() => {
          this.showRequestCard = false;
        }, 5000);
      },
      error: (error) => {
        this.feedbackMessage = "Failed to submit provider request. You may have already requested.";
        console.error('Provider request error:', error);
      }
    });
  }
}
