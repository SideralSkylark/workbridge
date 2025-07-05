import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../../../../environments/environment';
import { Observable } from 'rxjs';
import { ServiceFeedDTO } from '../models/service-feed.model';
import { Service } from '../../../provider/my-services/models/service.model';

@Injectable({
  providedIn: 'root'
})
export class FeedService {
  private apiUrl = `${environment.apiBaseUrl}/v1/services/feed`;

  constructor(
    private http: HttpClient) {}

  getServiceFeed(): Observable<ServiceFeedDTO[]> {
    return this.http.get<ServiceFeedDTO[]>(this.apiUrl);
  }

  getServiceDetails(serviceId: number): Observable<Service> {
    return this.http.get<Service>(`${environment.apiBaseUrl}/v1/services/${serviceId}`);
  }
}
