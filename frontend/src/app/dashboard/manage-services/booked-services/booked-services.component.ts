import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { BookingService } from '../../../services/bookings.service';
import { AuthService } from '../../../auth/auth.service';
import { BookingResponseDTO } from '../../../models/booking-responseDTO.model';
import { Router } from '@angular/router';
import { ChatService } from '../../../services/chat.service';

@Component({
  selector: 'app-booked-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './booked-services.component.html',
  styleUrls: ['./booked-services.component.scss']
})
export class BookedServicesComponent implements OnInit {
  isLoading = true;
  bookings: BookingResponseDTO[] = [];

  constructor(
    private bookingService: BookingService,
    private authService: AuthService,
    private router: Router,
    private chatService: ChatService
  ) {}

  ngOnInit() {
    const providerId = this.authService.getUserId(); // Get provider ID from AuthService

    if (providerId) {
      this.bookingService.getMyBookedServices(providerId).subscribe(
        (data: BookingResponseDTO[]) => {
          this.bookings = data;
          this.isLoading = false; // Data is fetched, set loading to false
        },
        (error) => {
          console.error('Error fetching bookings', error);
          this.isLoading = false; // Handle error and stop loading
        }
      );
    } else {
      console.error('No provider ID found');
      this.isLoading = false;
    }
  }

  openChat(customerId: string): void {
    this.chatService.ensureConnection(); // A gente vai criar isso também (pra resolver o problema 2)
    this.router.navigate(['/dashboard/chat'], {
      queryParams: { recipient: customerId }
    });
  }
}