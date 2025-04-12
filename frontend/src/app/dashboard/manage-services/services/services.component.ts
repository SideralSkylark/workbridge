import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';
import { ServiceService } from '../../../services/service.service';
import { Service } from '../../../models/service.model';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent {
  services: Service[] = [];
  bookings = [];

  constructor(private serviceService: ServiceService, router: Router) { }
  
  ngOnInit(): void {
    this.fetchServices();
  }

  fetchServices(): void {
    this.serviceService.getServicesByProvider().subscribe({
      next: (serviceDTOs) => {
        this.services = serviceDTOs.map(dto => ({
          id: dto.id, 
          title: dto.title,
          description: dto.description,
          price: dto.price,
          providerId: dto.providerId
        }));
      },
      error: (error) => {
        console.error('Failed to fetch services:', error);
      }
    });
  }  

  openForm(): void {
    console.log("Opening the form to add a new service");
  }

  editService(service: Service): void {
    console.log("Editing service:", service);
  }

  deleteService(serviceId: number): void {
    console.log("Deleting service with ID:", serviceId);
    this.serviceService.deleteService(serviceId).subscribe({
      next: () => {
        this.services = this.services.filter(service => service.id !== serviceId);
      },
      error: (error) => {
        console.error('Failed to delete service:', error);
      }
    });
  }

  openBookedServices(): void {
    console.log("Opening booked services");
  }

  viewBookings(): void {
    console.log("Viewing all bookings");
  }
}