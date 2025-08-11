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

  bookingRequest: BookingRequestDTO = { serviceId: 0, date: '' };

  loading = true;
  searchQuery = '';

  // New: active filters state
  activeFilters: string[] = [];
  get activeFiltersCount(): number {
    return this.activeFilters.length;
  }

  selectedService: ServiceFeedDTO | null = null;
  showModal = false;
  currentBookingId: number | null = null;
  hasBooked = false;
  isBookingInProgress = false;

  private destroy$ = new Subject<void>();
  private searchSubject = new Subject<string>();

  constructor(
    private feedService: FeedService,
    private bookingService: BookingService
  ) {
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

  loadServiceFeed(): void {
    this.loading = true;
    this.feedService.getServiceFeed()
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (data) => {
          this.services = data;
          this.filteredServices = data;
          this.loading = false;
          this.applyFiltersAndSearch();
        },
        error: (error) => {
          console.error('Error loading service feed:', error);
          this.loading = false;
          this.showErrorMessage('Erro ao carregar serviços. Tente novamente.');
        }
      });
  }

  // Called on input event
  filterServices(): void {
    this.searchSubject.next(this.searchQuery);
  }

  // Apply search + filters combined
  private applyFiltersAndSearch(): void {
    let filtered = [...this.services];

    // Apply text search
    if (this.searchQuery.trim()) {
      const term = this.searchQuery.toLowerCase().trim();
      filtered = filtered.filter(s =>
        s.service.title.toLowerCase().includes(term) ||
        s.service.description.toLowerCase().includes(term) ||
        s.providerUsername.toLowerCase().includes(term)
      );
    }

    // Apply active filters
    this.activeFilters.forEach(filter => {
      switch (filter) {
        // case 'Mais recentes':
        //   filtered = filtered.sort((a, b) =>
        //     new Date(b.service.createdAt).getTime() - new Date(a.service.createdAt).getTime()
        //   );
        //   break;
        case 'Mais populares':
          filtered = filtered.sort((a, b) => b.providerRating - a.providerRating);
          break;
        case 'Preço baixo':
          filtered = filtered.sort((a, b) => a.service.price - b.service.price);
          break;
        case 'Preço alto':
          filtered = filtered.sort((a, b) => b.service.price - a.service.price);
          break;
        // Add other filters here if needed
      }
    });

    this.filteredServices = filtered;
  }

  private performSearch(query: string): void {
    this.searchQuery = query; // sync just in case
    this.applyFiltersAndSearch();
  }

  // Filter management methods
  addFilter(filter: string): void {
    if (!this.activeFilters.includes(filter)) {
      this.activeFilters.push(filter);
      this.applyFiltersAndSearch();
    }
  }

  removeFilter(filter: string): void {
    this.activeFilters = this.activeFilters.filter(f => f !== filter);
    this.applyFiltersAndSearch();
  }

  clearAllFilters(): void {
    this.activeFilters = [];
    this.applyFiltersAndSearch();
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.applyFiltersAndSearch();
  }

  refreshFeed(): void {
    this.loadServiceFeed();
  }

  openModal(service: ServiceFeedDTO): void {
    this.selectedService = service;
    this.showModal = true;
    this.hasBooked = false;
    this.currentBookingId = null;
    this.checkExistingBooking(service.service.id);

    setTimeout(() => {
      const modalElement = document.querySelector('.modal-card');
      if (modalElement) (modalElement as HTMLElement).focus();
    }, 100);
  }

  closeModal(): void {
    this.showModal = false;
    this.selectedService = null;
    this.currentBookingId = null;
    this.hasBooked = false;
  }

  private checkExistingBooking(serviceId: number): void {
    this.bookingService.getLatestBookingByService(serviceId)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: booking => {
          this.currentBookingId = booking?.id || null;
          this.hasBooked = !!this.currentBookingId;
        },
        error: error => {
          console.error('Error fetching booking:', error);
          this.currentBookingId = null;
          this.hasBooked = false;
        }
      });
  }

  bookService(): void {
    if (!this.selectedService || this.hasBooked || this.isBookingInProgress) return;

    this.isBookingInProgress = true;
    this.bookingRequest.serviceId = this.selectedService.service.id;

    this.bookingService.createBooking(this.bookingRequest)
      .pipe(takeUntil(this.destroy$))
      .subscribe({
        next: (response) => {
          this.isBookingInProgress = false;
          const bookingResponse = Array.isArray(response) ? response[0] : response;

          if (bookingResponse && bookingResponse.id) {
            this.currentBookingId = bookingResponse.id;
            this.hasBooked = true;
            this.showSuccessMessage('Serviço agendado com sucesso! Pode deixar uma avaliação nos seus agendamentos.');

            setTimeout(() => this.closeModal(), 2000);
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

  closeServiceDetails(): void {
    this.selectedService = null;
    this.currentBookingId = null;
    this.hasBooked = false;
    this.showModal = false;
  }

  trackByServiceId(index: number, service: ServiceFeedDTO): number {
    return service.service.id;
  }

  onKeydown(event: KeyboardEvent): void {
    if (event.key === 'Escape' && this.showModal) {
      this.closeModal();
    }
  }

  private showSuccessMessage(message: string): void {
    alert(message);
    console.log('Success:', message);
  }

  private showErrorMessage(message: string): void {
    alert(message);
    console.error('Error:', message);
  }

  getServiceStatus(service: ServiceFeedDTO): 'available' | 'booked' | 'unavailable' {
    return this.hasBooked ? 'booked' : 'available';
  }

  formatPrice(price: number): string {
    return new Intl.NumberFormat('pt-MZ', {
      style: 'currency',
      currency: 'MZN',
      minimumFractionDigits: 0,
      maximumFractionDigits: 2
    }).format(price);
  }

  getRatingDescription(rating: number): string {
    if (rating >= 4.5) return 'Excelente';
    if (rating >= 4.0) return 'Muito bom';
    if (rating >= 3.5) return 'Bom';
    if (rating >= 3.0) return 'Regular';
    return 'Baixo';
  }

  isServiceBookable(service: ServiceFeedDTO): boolean {
    return !this.hasBooked && !this.isBookingInProgress && !!service;
  }

  getBookingButtonText(): string {
    if (this.isBookingInProgress) return 'Agendando...';
    if (this.hasBooked) return 'Já Agendado';
    return 'Agendar Serviço';
  }

  getBookingButtonIcon(): string {
    if (this.isBookingInProgress) return 'bi-hourglass-split';
    if (this.hasBooked) return 'bi-check-circle';
    return 'bi-calendar-plus';
  }
}
