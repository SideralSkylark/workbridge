import { Component, OnInit, OnDestroy } from '@angular/core';
import { FeedService } from './services/feed.service';
import { BookingService } from '../../provider/booked-services/services/bookings.service';
import { ServiceFeedDTO } from './models/service-feed.model';
import { BookingRequestDTO } from './models/booking-requestDTO.model';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Service } from '../../provider/my-services/models/service.model';
import { Subject, takeUntil, debounceTime, distinctUntilChanged } from 'rxjs';

@Component({
  selector: 'app-feed',
  templateUrl: './feed.component.html',
  styleUrls: ['./feed.component.scss'],
  standalone: true,
  imports: [CommonModule, FormsModule]
})
export class FeedComponent implements OnInit, OnDestroy {
  services: ServiceFeedDTO[] = [];
  filteredServices: ServiceFeedDTO[] = [];
  bookingRequest: BookingRequestDTO = {
    serviceId: 0,
    date: ''
  };
  loading = true;
  searchQuery = '';
  selectedService: ServiceFeedDTO | null = null;
  showModal: boolean = false;
  currentBookingId: number | null = null;
  hasBooked: boolean = false;
  isBookingInProgress: boolean = false;

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  constructor(
    private feedService: FeedService,
    private bookingService: BookingService
  ) {
    // Enhanced search with debouncing
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged(),
      takeUntil(this.destroy$)
    ).subscribe(query => {
      this.performSearch(query);
    });
  }

  ngOnInit(): void {
    this.loadServiceFeed();
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  /**
   * Load the service feed with error handling
   */
  loadServiceFeed(): void {
    this.loading = true;

    this.feedService.getServiceFeed()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.services = data;
          this.filteredServices = data;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading service feed:', error);
          this.loading = false;
          this.showErrorMessage('Erro ao carregar serviços. Tente novamente.');
        }
      });
  }

  /**
   * Enhanced search functionality with debouncing
   */
  filterServices(): void {
    this.searchSubject.next(this.searchQuery);
  }

  /**
   * Perform the actual search filtering
   */
  private performSearch(query: string): void {
    if (!query.trim()) {
      this.filteredServices = this.services;
      return;
    }

    const searchTerm = query.toLowerCase().trim();
    this.filteredServices = this.services.filter(service => {
      const titleMatch = service.service.title.toLowerCase().includes(searchTerm);
      const descriptionMatch = service.service.description.toLowerCase().includes(searchTerm);
      const providerMatch = service.providerUsername.toLowerCase().includes(searchTerm);

      return titleMatch || descriptionMatch || providerMatch;
    });
  }

  /**
   * Clear search and show all services
   */
  clearSearch(): void {
    this.searchQuery = '';
    this.filteredServices = this.services;
  }

  /**
   * Refresh the service feed
   */
  refreshFeed(): void {
    this.loadServiceFeed();
  }

  /**
   * Enhanced modal opening with better state management
   */
  openModal(service: ServiceFeedDTO): void {
    this.selectedService = service;
    this.showModal = true;
    this.hasBooked = false;
    this.currentBookingId = null;

    // Check if user has already booked this service
    this.checkExistingBooking(service.service.id);

    // Focus management for accessibility
    setTimeout(() => {
      const modalElement = document.querySelector('.modal-card');
      if (modalElement) {
        (modalElement as HTMLElement).focus();
      }
    }, 100);
  }

  /**
   * Close modal with proper cleanup
   */
  closeModal(): void {
    this.showModal = false;
    this.selectedService = null;
    this.currentBookingId = null;
    this.hasBooked = false;
  }

  /**
   * Check if user has existing booking for this service
   */
  private checkExistingBooking(serviceId: number): void {
    this.bookingService.getLatestBookingByService(serviceId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (booking) => {
          this.currentBookingId = booking?.id || null;
          this.hasBooked = !!this.currentBookingId;
        },
        error: (error) => {
          console.error('Error fetching booking:', error);
          this.currentBookingId = null;
          this.hasBooked = false;
        }
      });
  }

  /**
   * Enhanced booking functionality
   */
  bookService(): void {
    if (!this.selectedService || this.hasBooked || this.isBookingInProgress) {
      return;
    }

    this.isBookingInProgress = true;
    this.bookingRequest.serviceId = this.selectedService.service.id;

    this.bookingService.createBooking(this.bookingRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.isBookingInProgress = false;

          // Handle response (array or single object)
          const bookingResponse = Array.isArray(response) ? response[0] : response;

          if (bookingResponse && bookingResponse.id) {
            this.currentBookingId = bookingResponse.id;
            this.hasBooked = true;

            this.showSuccessMessage('Serviço agendado com sucesso! Pode deixar uma avaliação nos seus agendamentos.');

            // Auto-close modal after success
            setTimeout(() => {
              this.closeModal();
            }, 2000);
          } else {
            console.error('No booking ID returned from the server');
            this.showErrorMessage('Serviço agendado, mas houve um problema ao recuperar os detalhes.');
          }
        },
        error: (error) => {
          this.isBookingInProgress = false;
          console.error('Error booking service:', error);

          if (error.status === 409) {
            this.showErrorMessage('Já tem este serviço agendado.');
            this.hasBooked = true;
          } else if (error.status === 403) {
            this.showErrorMessage('Não tem permissão para agendar este serviço.');
          } else {
            this.showErrorMessage('Falha ao agendar serviço. Tente novamente.');
          }
        }
      });
  }

  /**
   * Load detailed service information
   */
  loadServiceDetails(serviceId: number): void {
    this.feedService.getServiceDetails(serviceId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (service: Service) => {
          if (this.selectedService) {
            this.selectedService.service = service;
            console.log('Updated Service:', service);
            console.log('Updated Provider ID:', service.providerId);
          }
        },
        error: (error: any) => {
          console.error('Error loading service details:', error);
          this.showErrorMessage('Erro ao carregar detalhes do serviço.');
        }
      });
  }

  /**
   * Close service details and reset state
   */
  closeServiceDetails(): void {
    this.selectedService = null;
    this.currentBookingId = null;
    this.hasBooked = false;
    this.showModal = false;
  }

  /**
   * Track by function for better performance in *ngFor
   */
  trackByServiceId(index: number, service: ServiceFeedDTO): number {
    return service.service.id;
  }

  /**
   * Handle keyboard events for accessibility
   */
  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.showModal) {
      this.closeModal();
    }
  }

  /**
   * Show success message to user
   * In production, consider using a toast service
   */
  private showSuccessMessage(message: string): void {
    // For now, using alert - implement toast notification service for better UX
    alert(message);
    console.log('Success:', message);
  }

  /**
   * Show error message to user
   * In production, consider using a toast service
   */
  private showErrorMessage(message: string): void {
    // For now, using alert - implement toast notification service for better UX
    alert(message);
    console.error('Error:', message);
  }

  /**
   * Get service availability status
   */
  getServiceStatus(service: ServiceFeedDTO): 'available' | 'booked' | 'unavailable' {
    // This would typically check against user's bookings or service availability
    return this.hasBooked ? 'booked' : 'available';
  }

  /**
   * Format price for display
   */
  formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-MZ', {
      style: 'currency',
      currency: 'MZN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(price);
  }

  /**
   * Get rating description for accessibility
   */
  getRatingDescription(rating: number): string {
    if (rating >= 4.5) return 'Excelente';
    if (rating >= 4.0) return 'Muito bom';
    if (rating >= 3.5) return 'Bom';
    if (rating >= 3.0) return 'Regular';
    return 'Baixo';
  }

  /**
   * Check if service is bookable
   */
  isServiceBookable(service: ServiceFeedDTO): boolean {
    return !this.hasBooked && !this.isBookingInProgress && !!service;
  }

  /**
   * Get button text based on state
   */
  getBookingButtonText(): string {
    if (this.isBookingInProgress) return 'Agendando...';
    if (this.hasBooked) return 'Já Agendado';
    return 'Agendar Serviço';
  }

  /**
   * Get button icon based on state
   */
  getBookingButtonIcon(): string {
    if (this.isBookingInProgress) return 'bi-hourglass-split';
    if (this.hasBooked) return 'bi-check-circle';
    return 'bi-calendar-plus';
  }
}
