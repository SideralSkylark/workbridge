import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../../../environments/environment';

export interface ReviewRequestDTO {
  rating: number;
  comment: string;
  bookingId: number;
  reviewedId: number;
  reviewerId: number;
}

export interface ReviewResponseDTO {
  id: number;
  rating: number;
  comment: string;
  reviewer: any;
  reviewed: any;
  createdAt: string;
}

@Injectable({
  providedIn: 'root'
})
export class ReviewService {
  private apiUrl = `${environment.apiBaseUrl}/v1/reviews`;

  constructor(private http: HttpClient) { }

  getReviewsByProvider(providerId: number): Observable<ReviewResponseDTO[]> {
    return this.http.post<ReviewResponseDTO[]>(`${this.apiUrl}`, { reviewedId: providerId });
  }

  createReview(review: ReviewRequestDTO): Observable<ReviewResponseDTO> {
    return this.http.post<ReviewResponseDTO>(`${this.apiUrl}/review`, review);
  }

  hasUserReviewedBooking(bookingId: number): Observable<boolean> {
    return this.http.get<boolean>(`${this.apiUrl}/check/${bookingId}`);
  }
}
