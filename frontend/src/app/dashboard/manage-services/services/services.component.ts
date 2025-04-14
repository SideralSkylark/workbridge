import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterOutlet } from '@angular/router';
import { AuthService } from '../../../auth/auth.service';
import { ServiceService } from '../../../services/service.service';
import { Service } from '../../../models/service.model';
import { CreateServiceDTO } from '../../../models/createServiceDTO.model';
import { ServiceFormStateService } from '../../../services/service-form-state.service';

@Component({
  selector: 'app-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet, FormsModule],
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.scss']
})
export class ServicesComponent implements OnInit {
  services: Service[] = [];
  showForm: boolean = false;
  newService!: CreateServiceDTO;
  editingServiceId: number | null = null;
  isLoading: boolean = true; 

  constructor(
    private authService: AuthService,
    private serviceService: ServiceService,
    private formStateService: ServiceFormStateService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.initNewService();
    this.fetchServices();

    this.formStateService.formVisibility$.subscribe((visible: boolean) => {
      this.showForm = visible;
      if (visible && this.editingServiceId === null) this.initNewService();
    });
  }

  initNewService(): void {
    const providerId = this.authService.getUserId();
    if (providerId === null) {
      console.error('Provider ID is null – user may not be logged in');
      return;
    }

    this.newService = {
      title: '',
      description: '',
      price: 0,
      providerId: providerId
    };
  }

  fetchServices(): void {
    this.isLoading = true;
    this.serviceService.getServicesByProvider().subscribe({
      next: (serviceDTOs) => {
        this.services = serviceDTOs.map(dto => ({
          id: dto.id,
          title: dto.title,
          description: dto.description,
          price: dto.price,
          providerId: dto.providerId
        }));
        this.isLoading = false;
      },
      error: (error) => {
        console.error('Failed to fetch services:', error);
        this.isLoading = false;
      }
    });
  }

  openForm(): void {
    this.editingServiceId = null; 
    this.initNewService();
    this.showForm = true;
  }

  cancelForm(): void {
    this.showForm = false;
    this.editingServiceId = null;
    this.formStateService.hideForm();
    this.resetForm();
  }

  resetForm(): void {
    this.initNewService();
  }

  submitForm(): void {
    if (this.editingServiceId !== null) {
      const updatedService = {
        ...this.newService,
        id: this.editingServiceId
      };

      this.serviceService.updateService(this.editingServiceId, updatedService).subscribe({
        next: (res) => {
          const index = this.services.findIndex(s => s.id === this.editingServiceId);
          if (index !== -1) this.services[index] = res;

          this.cancelForm();
        },
        error: (error) => {
          console.error('Failed to update service:', error);
        }
      });
    } else {
      const serviceToSend = { ...this.newService };

      this.serviceService.createService(serviceToSend).subscribe({
        next: (createdService) => {
          this.services.push(createdService);
          this.cancelForm();
        },
        error: (error) => {
          console.error('Failed to create service:', error);
        }
      });
    }
  }

  editService(service: Service): void {
    this.editingServiceId = service.id!;
    this.newService = {
      title: service.title,
      description: service.description,
      price: service.price,
      providerId: service.providerId
    };

    this.showForm = true;
  }

  deleteService(serviceId: number): void {
    this.serviceService.deleteService(serviceId).subscribe({
      next: () => {
        this.services = this.services.filter(service => service.id !== serviceId);
      },
      error: (error) => {
        console.error('Failed to delete service:', error);
      }
    });
  }

  openBookedServices(): void {
    console.log("Opening booked services");
  }

  viewBookings(): void {
    console.log("Viewing all bookings");
  }
}