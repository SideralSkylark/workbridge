import { Component, OnInit } from '@angular/core';
import { FeedService } from '../../../services/feed.service';
import { ServiceFeedDTO } from '../../../models/service-feed.model'; // updated import
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class FeedComponent implements OnInit {
  services: ServiceFeedDTO[] = [];
  filteredServices: ServiceFeedDTO[] = [];
  loading = true;
  searchQuery = '';
  selectedService: any = null;

  constructor(private feedService: FeedService) {}

  ngOnInit(): void {
    this.feedService.getServiceFeed().subscribe({
      next: (data) => {
        this.services = data;
        this.filteredServices = data;
        this.loading = false;
      },
      error: () => {
        this.loading = false;
      }
    });
  }

  searchServices(): void {
    const q = this.searchQuery.toLowerCase().trim();
    this.filteredServices = this.services.filter(feed =>
      feed.service.title.toLowerCase().includes(q) ||
      feed.service.description.toLowerCase().includes(q)
    );
  }

  openModal(service: ServiceFeedDTO): void {
    
  }
}