import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../services/user.service';
import { ProviderRequest } from './models/provider-requestDTO.model';

@Component({
  selector: 'app-aprove-providers',
  imports: [CommonModule],
  standalone: true,
  templateUrl: './aprove-providers.component.html',
  styleUrl: './aprove-providers.component.scss'
})
export class AproveProvidersComponent implements OnInit {
  requests: ProviderRequest[] = [];
  isLoading: boolean = true;
  showSuccessCard: boolean = false;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.fetchRequests();
  }

  fetchRequests(): void {
    this.isLoading = true;
    this.userService.getUnapprovedProviderRequests().subscribe({
      next: (data) => {
        this.requests = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching provider requests:', err);
        this.isLoading = false;
      }
    });
  }

  //TODO: solve the issue of page not updating its state after aproval
  approveRequest(requestId: number): void {
    this.userService.approveProviderRequest(requestId).subscribe({
      next: (message) => {
        console.log(message);

      this.requests = this.requests.filter(req => req.id !== requestId);

      this.showSuccessCard = true;

      setTimeout(() => {
        this.showSuccessCard = false;
      }, 3000);
      },
      error: (err) => {
        console.error('Failed to approve request:', err);
      }
    });
  }
}
