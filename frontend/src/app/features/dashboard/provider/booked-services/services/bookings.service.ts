import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';
import { Observable } from 'rxjs';
import { BookingRequestDTO } from '../../../seeker/feed/models/booking-requestDTO.model';
import { BookingResponseDTO } from '../models/booking-responseDTO.model';
import { map } from 'rxjs/operators';

export interface Booking {
  id: number;
  serviceId: number;
  date: string;
  status: string;
}

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private baseUrl = `${environment.apiBaseUrl}/v1/bookings`;

  constructor(private http: HttpClient) {}

  getMyBookings(): Observable<BookingResponseDTO[]> {
    return this.http.get<BookingResponseDTO[]>(`${this.baseUrl}/me`);
  }

  getMyBookedServices(providerId: number): Observable<BookingResponseDTO[]> {
    return this.http.get<BookingResponseDTO[]>(`${this.baseUrl}/provider`, {
      params: {providerId: providerId.toString()}
    });
  }



  createBooking(bookingRequest: BookingRequestDTO): Observable<BookingResponseDTO> {
    return this.http.post<BookingResponseDTO>(`${this.baseUrl}/book`, bookingRequest);
  }

  cancelBooking(bookingId: number): Observable<any> {
    return this.http.post(`${this.baseUrl}/cancel`, null, {
      params: { bookingId: bookingId.toString() }
    });
  }

  getLatestBookingByService(serviceId: number): Observable<Booking | null> {
    return this.http.get<Booking[]>(`${this.baseUrl}/me`).pipe(
      map(bookings => {
        // Find the most recent booking for this service
        const serviceBookings = bookings
          .filter(b => b.serviceId === serviceId)
          .sort((a, b) => new Date(b.date).getTime() - new Date(a.date).getTime());
        return serviceBookings.length > 0 ? serviceBookings[0] : null;
      })
    );
  }
}
