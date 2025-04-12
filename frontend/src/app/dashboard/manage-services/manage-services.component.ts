import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../../services/service-form-state.service';

@Component({
  selector: 'app-manage-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './manage-services.component.html',
  styleUrl: './manage-services.component.scss'
})
export class ManageServicesComponent {

  constructor(
    private router: Router,
    private formStateService: ServiceFormStateService
  ) {}

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
}
