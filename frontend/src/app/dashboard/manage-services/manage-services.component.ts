import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../../services/service-form-state.service';
import { filter } from 'rxjs';

@Component({
  selector: 'app-manage-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './manage-services.component.html',
  styleUrl: './manage-services.component.scss'
})
export class ManageServicesComponent {
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

  openForm(): void {
    this.router.navigate(['dashboard', 'manage', 'services']).then(() => {
      this.formStateService.showForm(); 
    });
  }

  viewServices(): void {
    this.router.navigate(['dashboard', 'manage', 'services']); 
  }

  viewBookings(): void {
    this.router.navigate(['dashboard', 'manage', 'bookings']);
  }

  isOnServicesPage(): boolean {
    return this.currentRoute.endsWith('/services');
  }
}
