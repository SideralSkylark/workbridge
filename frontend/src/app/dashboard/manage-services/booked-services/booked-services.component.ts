import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterOutlet } from '@angular/router';

@Component({
  selector: 'app-booked-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './booked-services.component.html',
  styleUrl: './booked-services.component.scss'
})
export class BookedServicesComponent {
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
}
