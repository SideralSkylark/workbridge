import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../../services/service-form-state.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-service-requests',
  standalone: true,
  imports: [RouterOutlet, CommonModule],
  templateUrl: './service-requests.component.html',
  styleUrl: './service-requests.component.scss'
})
export class ServiceRequestsComponent {
  currentRoute: string = '';

  constructor(
    private router: Router,
    private formStateService: ServiceFormStateService
  ) {
    this.currentRoute = this.router.url;

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
    });
  }

  viewFeed(): void {
    this.router.navigate(['dashboard', 'requests', 'feed']);
  }

  viewBookings(): void {
    this.router.navigate(['dashboard', 'requests', 'bookings']);
  }
}
