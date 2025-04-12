import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';
import { Observable } from 'rxjs';

export interface ServiceDTO {
  id: number;
  title: string;
  description: string;
  price: number;
  providerId: number;
}

@Injectable({
  providedIn: 'root'
})
export class ServiceService {
  private apiUrl = `${environment.apiBaseUrl}/v1/services`;

  constructor(private http: HttpClient) {}

  /** Create a new service */
  createService(service: ServiceDTO): Observable<ServiceDTO> {
    return this.http.post<ServiceDTO>(`${this.apiUrl}`, service);
  }

  /** Get all services for the currently authenticated provider */
  getServicesByProvider(): Observable<ServiceDTO[]> {
    return this.http.get<ServiceDTO[]>(`${this.apiUrl}/me`);
  }

  /** Get a specific service by its ID */
  getServiceById(serviceId: number): Observable<ServiceDTO> {
    return this.http.get<ServiceDTO>(`${this.apiUrl}/${serviceId}`);
  }

  /** Update a service by ID */
  updateService(serviceId: number, updatedService: ServiceDTO): Observable<ServiceDTO> {
    return this.http.put<ServiceDTO>(`${this.apiUrl}/${serviceId}`, updatedService);
  }

  /** Delete a service by ID */
  deleteService(serviceId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${serviceId}`);
  }
}
