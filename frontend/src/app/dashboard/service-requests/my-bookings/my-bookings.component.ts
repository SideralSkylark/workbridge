import { Component, OnInit } from '@angular/core';
import { BookingService } from '../../../services/bookings.service';
import { BookingResponseDTO } from '../../../models/booking-responseDTO.model';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-my-bookings',
  imports: [CommonModule],
  templateUrl: './my-bookings.component.html',
  styleUrl: './my-bookings.component.scss',
  standalone: true
})
export class MyBookingsComponent implements OnInit {
  bookings: BookingResponseDTO[] = [];
  loading = true;
  errorMessage = '';

  constructor(private bookingService: BookingService) {}

  ngOnInit(): void {
    this.bookingService.getMyBookings().subscribe({
      next: (response) => {
        this.bookings = Array.isArray(response) ? response : [response]; 
        this.loading = false;
      },
      error: (err) => {
        this.errorMessage = 'Failed to load bookings.';
        this.loading = false;
      }
    });
  }
}