import { Component, OnInit } from '@angular/core';
import { BookingService } from '../../../services/bookings.service';
import { BookingResponseDTO } from '../../../models/booking-responseDTO.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.scss']
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingResponseDTO[] = [];
  loading = true;
  errorMessage = '';

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    this.loadMyBookings();
  }

  loadMyBookings(): void {
    this.loading = true;
    this.bookingService.getMyBookings().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bookings.';
        this.loading = false;
        console.error('Error loading bookings:', err);
      }
    });
  }

  cancelBooking(bookingId: number): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.bookingService.cancelBooking(bookingId).subscribe({
        next: (response) => {
          alert(response.message || 'Booking canceled successfully.');
          this.loadMyBookings();
        },
        error: (error) => {
          console.error('Cancellation error:', error);
          const errorMessage = error?.error?.message || 'An error occurred while canceling the booking.';
          alert(errorMessage);
        }
      });
    }
  }

  openChat(): void {
    // You can implement chat redirection here
  }
}