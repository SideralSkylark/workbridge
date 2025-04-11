import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent {
  bookings = [
    {
      serviceTitle: 'Website Design',
      date: '2025-04-15',
      customerName: 'John Doe'
    }
  ];
  
  services = [
    {
      id: 1,
      title: 'Website Design',
      description: 'Professional responsive websites tailored to your brand.',
      price: 750.00,
    },
    {
      id: 2,
      title: 'SEO Optimization',
      description: 'Improve your site ranking on search engines.',
      price: 450.00,
    },
    {
      id: 3,
      title: 'Social Media Management',
      description: 'Daily posting and audience engagement on Instagram and Facebook.',
      price: 650.00,
    },
    {
      id: 4,
      title: 'Logo Design',
      description: 'Minimalist and modern logos delivered in multiple formats.',
      price: 200.00,
    },
    {
      id: 5,
      title: 'eCommerce Setup',
      description: 'Complete WooCommerce/Shopify setup and configuration.',
      price: 1200.00,
    }
  ];
  
  // Opens a form to add a new service
  openForm(): void {
    console.log("Opening the form to add a new service");
  }

  // Edit a service
  editService(service: any): void {
    console.log("Editing service:", service);
  }

  // Delete a service
  deleteService(serviceId: number): void {
    console.log("Deleting service with ID:", serviceId);
    this.services = this.services.filter(service => service.id !== serviceId); // Remove the service from the array
  }

  // Open booked services
  openBookedServices(): void {
    console.log("Opening booked services");
  }

  // View bookings
  viewBookings(): void {
    console.log("Viewing all bookings");
  }
}