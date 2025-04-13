import { Component, OnInit } from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import { BookingService } from '../../../services/bookings.service';
import { ServiceFeedDTO } from '../../../models/service-feed.model';
import { BookingRequestDTO } from '../../../models/booking-requestDTO.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class FeedComponent implements OnInit {
  services: ServiceFeedDTO[] = [];
  filteredServices: ServiceFeedDTO[] = [];
  bookingRequest: BookingRequestDTO = {
    serviceId: 0,
    date: ''
  };
  loading = true;
  searchQuery = '';
  selectedService: ServiceFeedDTO | null = null;
  showModal: boolean = false;

  constructor(
    private feedService: FeedService,
    private bookingService: BookingService
  ) {}

  ngOnInit(): void {
    this.feedService.getServiceFeed().subscribe({
      next: (data) => {
        this.services = data;
        this.filteredServices = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  searchServices(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredServices = this.services.filter(feed =>
      feed.service.title.toLowerCase().includes(q) ||
      feed.service.description.toLowerCase().includes(q)
    );
  }

  openModal(service: ServiceFeedDTO): void {
    this.selectedService = service;
    this.showModal = true;
  }

  closeModal(): void {
    this.selectedService = null;
    this.showModal = false;
  }

  bookService(): void {
    if (!this.selectedService) return;
  
    const isoDate = new Date().toISOString(); // current date/time
  
    this.bookingRequest = {
      serviceId: this.selectedService.service.id,
      date: isoDate
    };
  
    this.bookingService.createBooking(this.bookingRequest).subscribe({
      next: (res) => {
        alert('Booking created successfully!');
        this.closeModal();
      },
      error: (err) => {
        console.error('Error creating booking:', err);
        alert('Failed to create booking.');
      }
    });
  }  
}