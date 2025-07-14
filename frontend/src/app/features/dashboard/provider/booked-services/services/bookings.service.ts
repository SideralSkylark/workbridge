import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';
import { Observable } from 'rxjs';
import { BookingRequestDTO } from '../../../seeker/feed/models/booking-requestDTO.model';
import { BookingResponseDTO } from '../models/booking-responseDTO.model';
import { map } from 'rxjs/operators';
import { ApiResponse } from '../../../../../shared/models/api-response.model';
import { PageModel } from '../../../../../shared/models/page-model.model';
import { response } from 'express';

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
    return this.http.get<ApiResponse<PageModel<BookingResponseDTO>>>(`${this.baseUrl}/seeker`).pipe(
      map(response => response.data.content)
    );
  }

  getMyBookedServices(): Observable<BookingResponseDTO[]> {
    return this.http.get<ApiResponse<PageModel<BookingResponseDTO>>>(`${this.baseUrl}/provider`).pipe(
      map(response => response.data.content)
    );
  }



  createBooking(bookingRequest: BookingRequestDTO): Observable<BookingResponseDTO> {
    return this.http.post<ApiResponse<BookingResponseDTO>>(`${this.baseUrl}/book`, bookingRequest).pipe(
      map(response => response.data)
    );
  }

  cancelBooking(bookingId: number): Observable<any> {
    return this.http.post<ApiResponse<string>>(`${this.baseUrl}/${bookingId}/cancel`, null).pipe(
      map(response => response.data)
    );
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
