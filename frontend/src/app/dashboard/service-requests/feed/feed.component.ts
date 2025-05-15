import { Component, OnInit } from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import { BookingService } from '../../../services/bookings.service';
import { ServiceFeedDTO } from '../../../models/service-feed.model';
import { BookingRequestDTO } from '../../../models/booking-requestDTO.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Service } from '../../../models/service.model';

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
  currentBookingId: number | null = null;
  hasBooked: boolean = false;

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

  filterServices(): void {
    if (!this.searchQuery.trim()) {
      this.filteredServices = this.services;
      return;
    }

    this.filteredServices = this.services.filter(service =>
      service.service.title.toLowerCase().includes(this.searchQuery.toLowerCase()) ||
      service.service.description.toLowerCase().includes(this.searchQuery.toLowerCase())
    );
  }

  openModal(service: ServiceFeedDTO): void {
    this.selectedService = service;
    this.showModal = true;
    this.hasBooked = false;
    
    // Get the latest booking for this service
    this.bookingService.getLatestBookingByService(service.service.id).subscribe({
      next: (booking) => {
        this.currentBookingId = booking?.id || null;
        if (this.currentBookingId) {
          this.hasBooked = true;
        }
      },
      error: (error) => {
        console.error('Error fetching booking:', error);
        this.currentBookingId = null;
      }
    });
  }

  closeModal(): void {
    this.showModal = false;
    // Don't reset the booking state when closing the modal
  }

  bookService(): void {
    if (this.selectedService) {
      this.bookingRequest.serviceId = this.selectedService.service.id;
      this.bookingService.createBooking(this.bookingRequest).subscribe({
        next: (response) => {
          // Check if response is an array or a single object
          const bookingResponse = Array.isArray(response) ? response[0] : response;
          
          if (bookingResponse && bookingResponse.id) {
            // Get the ID of the newly created booking
            this.currentBookingId = bookingResponse.id;
            this.hasBooked = true;
            console.log('New Booking ID:', this.currentBookingId);
            
            // Show success message
            alert('Service booked successfully! You can now leave a review in your bookings.');
          } else {
            console.error('No booking ID returned from the server');
            alert('Service booked, but there was an issue retrieving the booking details.');
          }
        },
        error: (error) => {
          console.error('Error booking service:', error);
          alert('Failed to book service. Please try again.');
        }
      });
    }
  }

  loadServiceDetails(serviceId: number): void {
    this.feedService.getServiceDetails(serviceId).subscribe({
      next: (service: Service) => {
        if (this.selectedService) {
          this.selectedService.service = service;
          console.log('Updated Service:', service);
          console.log('Updated Provider ID:', service.providerId);
        }
      },
      error: (error: any) => {
        console.error('Error loading service details:', error);
      }
    });
  }

  closeServiceDetails(): void {
    this.selectedService = null;
    this.currentBookingId = null;
    this.hasBooked = false;
  }
}