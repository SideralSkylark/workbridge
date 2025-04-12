import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-manage-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './manage-services.component.html',
  styleUrl: './manage-services.component.scss'
})
export class ManageServicesComponent {

  constructor(private router: Router) { }

  openForm(): void { }

  viewServices(): void {
    this.router.navigate(['dashboard', 'manage', 'services']); 
  }

  viewBookings(): void {
    this.router.navigate(['dashboard', 'manage', 'bookings']);
  }
}
