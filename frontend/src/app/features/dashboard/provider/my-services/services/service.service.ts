import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';
import { map, Observable } from 'rxjs';

import { CreateServiceDTO } from '../models/createServiceDTO.model';
import { ApiResponse } from '../../../../../shared/models/api-response.model';
import { PageModel } from '../../../../../shared/models/page-model.model';
import { response } from 'express';

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
  createService(service: CreateServiceDTO): Observable<ServiceDTO> {
    return this.http.post<ApiResponse<ServiceDTO>>(`${this.apiUrl}`, service).pipe(
      map(response => response.data)
    );
  }

  /** Get all services for the currently authenticated provider */
  getServicesByProvider(): Observable<ServiceDTO[]> {
    return this.http.get<ApiResponse<PageModel<ServiceDTO>>>(`${this.apiUrl}/provider/me`).pipe(
      map(response => response.data.content)
    );
  }

  /** Get a specific service by its ID */
  getServiceById(serviceId: number): Observable<ServiceDTO> {
    return this.http.get<ServiceDTO>(`${this.apiUrl}/${serviceId}`);
  }

  /** Update a service by ID */
  updateService(serviceId: number, updatedService: CreateServiceDTO): Observable<ServiceDTO> {
    return this.http.put<ApiResponse<ServiceDTO>>(`${this.apiUrl}/${serviceId}`, updatedService).pipe(
      map(response => response.data)
    );
  }

  /** Delete a service by ID */
  deleteService(serviceId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${serviceId}`);
  }
}
