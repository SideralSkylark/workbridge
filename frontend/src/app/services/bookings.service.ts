import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';
import { BookingRequestDTO } from '../models/booking-requestDTO.model';
import { BookingResponseDTO } from '../models/booking-responseDTO.model';

@Injectable({
  providedIn: 'root'
})
export class BookingService {
  private baseUrl = `${environment.apiBaseUrl}/v1/bookings`;

  constructor(private http: HttpClient) {}

  getMyBookings(): Observable<BookingResponseDTO> {
    return this.http.get<BookingResponseDTO>(`${this.baseUrl}/me`);
  }

  getMyBookedServices(providerId: number): Observable<BookingResponseDTO[]> {
    return this.http.get<BookingResponseDTO[]>(`${this.baseUrl}/provider`, {
      params: {providerId: providerId.toString()}
    });
  }

  createBooking(bookingRequest: BookingRequestDTO): Observable<BookingResponseDTO[]> {
    return this.http.post<BookingResponseDTO[]>(`${this.baseUrl}/book`, bookingRequest);
  }


}