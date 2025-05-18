import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { ServiceFormStateService } from '../../services/service-form-state.service';
import { filter } from 'rxjs';
import { HttpClient, HttpHeaders } from '@angular/common/http';

@Component({
  selector: 'app-manage-services',
  standalone: true,
  imports: [CommonModule, RouterOutlet],
  templateUrl: './manage-services.component.html',
  styleUrl: './manage-services.component.scss'
})
export class ManageServicesComponent {
  currentRoute: string = '';

  constructor(
    private router: Router,
    private formStateService: ServiceFormStateService,
    private http: HttpClient  
  ) {
    this.currentRoute = this.router.url;

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
    });
  }

  openForm(): void {
    this.router.navigate(['dashboard', 'manage', 'services']).then(() => {
      this.formStateService.showForm(); 
    });
  }

  viewServices(): void {
    this.router.navigate(['dashboard', 'manage', 'services']); 
  }

  viewBookings(): void {
    this.router.navigate(['dashboard', 'manage', 'bookings']);
  }

  openBillBridge(): void {
    const token = localStorage.getItem('jwt');
    const userJson = localStorage.getItem('user');
  
    if (!token || !userJson) {
      console.error('Token JWT ou usuário não encontrado.');
      return;
    }
  
    try {
      const userObj = JSON.parse(userJson);
 
      const minimalUser = {
        username: userObj.username,
        email: userObj.email
      };
  
      const userParam = encodeURIComponent(JSON.stringify(minimalUser));
      const url = `http://localhost:8081/dashboard?token=${encodeURIComponent(token)}&user=${userParam}`;
  
      window.open(url, '_blank');
    } catch (e) {
      console.error('Erro ao parsear usuário:', e);
    }
  }
  
  

  isOnServicesPage(): boolean {
    return this.currentRoute.endsWith('/services');
  }
}
