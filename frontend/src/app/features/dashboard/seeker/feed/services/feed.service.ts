import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';
import { map, Observable } from 'rxjs';
import { ServiceFeedDTO } from '../models/service-feed.model';
import { Service } from '../../../provider/my-services/models/service.model';
import { ApiResponse } from '../../../../../shared/models/api-response.model';
import { PageModel } from '../../../../../shared/models/page-model.model';
import { response } from 'express';

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private apiUrl = `${environment.apiBaseUrl}/v1/services`;

  constructor(
    private http: HttpClient) {}

  getServiceFeed(): Observable<ServiceFeedDTO[]> {
    return this.http.get<ApiResponse<PageModel<ServiceFeedDTO>>>(`${this.apiUrl}/feed`).pipe(
      map(response => response.data.content)
    );
  }

  getServiceDetails(serviceId: number): Observable<Service> {
    return this.http.get<ApiResponse<Service>>(`${environment.apiBaseUrl}/${serviceId}`).pipe(
      map(response => response.data)
    );
  }
}
