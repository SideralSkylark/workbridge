import { Component, OnInit } from '@angular/core';
import { BookingService } from '../../provider/booked-services/services/bookings.service';
import { BookingResponseDTO } from '../../provider/booked-services/models/booking-responseDTO.model';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { ChatService } from '../../chat/services/chat.service';
import { ReviewComponent } from '../../../../shared/components/review/review.component';
import { ReviewService } from './services/review.service';
import { AuthService } from '../../../auth/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-my-bookings',
  standalone: true,
  imports: [CommonModule, ReviewComponent, FormsModule],
  templateUrl: './my-bookings.component.html',
  styleUrls: ['./my-bookings.component.scss']
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingResponseDTO[] = [];
  loading = true;
  errorMessage = '';
  selectedBooking: BookingResponseDTO | null = null;
  showReviewModal = false;
  hasReviewed: { [key: number]: boolean } = {};
  providerIds: { [key: number]: number } = {};

  constructor(
    private bookingService: BookingService,
    private router: Router,
    private chatService: ChatService,
    private reviewService: ReviewService,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    this.loadMyBookings();
    console.log("provider ids:")
    console.log( this.providerIds);
  }

  loadMyBookings(): void {
    this.loading = true;
    this.bookingService.getMyBookings().subscribe({
      next: (bookings) => {
        this.bookings = bookings;
        this.loading = false;
        console.log("Bookings from API:", bookings);
        // Check if each booking has been reviewed
        bookings.forEach(booking => {
          this.checkIfReviwed(booking.id);
          // Store provider ID for each booking
          this.providerIds[booking.id] = booking.providerId;
        });
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bookings.';
        this.loading = false;
        console.error('Error loading bookings:', err);
      }
    });
  }

  checkIfReviwed(bookingId: number): void {
    this.reviewService.hasUserReviewedBooking(bookingId).subscribe({
      next: (hasReviewed) => {
        this.hasReviewed[bookingId] = hasReviewed;
      },
      error: (error) => {
        console.error('Error checking if booking was reviewed:', error);
        this.hasReviewed[bookingId] = false;
      }
    });
  }

  openReviewModal(booking: BookingResponseDTO): void {
    console.log('Opening review modal for booking:', booking);
    this.selectedBooking = booking;
    this.showReviewModal = true;

    // Make sure providerId is set
    if (!this.providerIds[booking.id]) {
      this.providerIds[booking.id] = booking.providerId;
    }

    console.log('Provider ID for booking:', this.providerIds[booking.id]);
  }

  closeReviewModal(): void {
    this.showReviewModal = false;
    this.selectedBooking = null;
  }

  onReviewSubmitted(): void {
    if (this.selectedBooking) {
      this.hasReviewed[this.selectedBooking.id] = true;
    }
    this.closeReviewModal();
  }

  cancelBooking(bookingId: number): void {
    if (confirm('Are you sure you want to cancel this booking?')) {
      this.bookingService.cancelBooking(bookingId).subscribe({
        next: (response) => {
          alert(response.message || 'Booking canceled successfully.');
          this.loadMyBookings();
        },
        error: (error) => {
          console.error('Error canceling booking:', error);
          alert('Failed to cancel booking. Please try again.');
        }
      });
    }
  }

  openChat(providerName: string): void {
    this.chatService.ensureConnection();
    this.router.navigate(['/dashboard/chat'], {
      queryParams: { recipient: providerName }
    });
  }
}
