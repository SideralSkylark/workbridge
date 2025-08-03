import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../provider/services/service-form-state.service';
import { AuthService } from '../../auth/auth.service';
import { filter, Subject, takeUntil } from 'rxjs';
import { UserService } from '../admin/services/user.service';

@Component({
  selector: 'app-service-requests',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './seeker.component.html',
  styleUrl: './seeker.component.scss'
})
export class ServiceRequestsComponent implements OnInit, OnDestroy {
  currentRoute: string = '';
  providerRequestSent: boolean = false;
  feedbackMessage: string = '';
  isProvider: boolean = false;
  showRequestCard: boolean = false;
  isLoading: boolean = false;

  private destroy$ = new Subject<void>();

  constructor(
    private router: Router,
    private formStateService: ServiceFormStateService,
    private authService: AuthService,
    private userService: UserService
  ) {
    this.currentRoute = this.router.url;

    // Enhanced router subscription with proper cleanup
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd),
      takeUntil(this.destroy$)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
    });
  }

  ngOnInit(): void {
    this.checkProviderStatus();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Check the current provider status of the user
   */
  checkProviderStatus(): void {
    this.userService.getProviderRequestStatus()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (status) => {
          this.providerRequestSent = status.requested;
          this.isProvider = status.approved;
        },
        error: (err) => {
          console.error("Error checking provider status", err);
          this.feedbackMessage = "Erro ao verificar status de provedor";
        }
      });
  }

  /**
   * Navigate to the services feed
   */
  viewFeed(): void {
    this.router.navigate(['dashboard', 'requests', 'feed']);
  }

  /**
   * Navigate to user's bookings
   */
  viewBookings(): void {
    this.router.navigate(['dashboard', 'requests', 'bookings']);
  }

  /**
   * Check if user is already a service provider
   */
  isServiceProvider(): boolean {
    return this.isProvider;
  }

  /**
   * Request to become a service provider
   */
  requestToBecomeProvider(): void {
    if (this.providerRequestSent || this.isProvider || this.isLoading) {
      return;
    }

    this.isLoading = true;

    this.userService.requestToBecomeProvider()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: () => {
          this.providerRequestSent = true;
          this.showRequestCard = true;
          this.feedbackMessage = "Requisição para se tornar provedor enviada com sucesso.";
          this.isLoading = false;

          // Refresh provider status
          this.checkProviderStatus();

          // Auto-close after 5 seconds
          setTimeout(() => {
            this.closeRequestCard();
          }, 5000);
        },
        error: (error) => {
          this.isLoading = false;
          console.error('Provider request error:', error);

          // Handle different error scenarios
          if (error.status === 409) {
            this.feedbackMessage = "Você já enviou uma requisição para se tornar provedor.";
            this.providerRequestSent = true;
          } else if (error.status === 403) {
            this.feedbackMessage = "Você já é um provedor de serviços.";
            this.isProvider = true;
          } else {
            this.feedbackMessage = "Falha ao enviar requisição. Tente novamente.";
          }

          // Show error feedback (you might want to implement a toast service)
          this.showErrorFeedback();
        }
      });
  }

  /**
   * Close the confirmation card
   */
  closeRequestCard(): void {
    this.showRequestCard = false;
  }

  /**
   * Show error feedback to user
   * This is a simple implementation - consider using a toast service for better UX
   */
  private showErrorFeedback(): void {
    // For now, just log - implement toast notification service for production
    console.error(this.feedbackMessage);

    // You could also show a temporary error message
    // this.showErrorMessage = true;
    // setTimeout(() => this.showErrorMessage = false, 3000);
  }

  /**
   * Get current route information for conditional styling
   */
  getCurrentRouteInfo(): { isFeed: boolean; isBookings: boolean } {
    return {
      isFeed: this.currentRoute.includes('/feed'),
      isBookings: this.currentRoute.includes('/bookings')
    };
  }

  /**
   * Handle keyboard navigation for accessibility
   */
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.showRequestCard) {
      this.closeRequestCard();
    }
  }
}
