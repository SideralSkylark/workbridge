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
    if (!token) {
      console.error('Token JWT não encontrado.');
      return;
    }

    const headers = new HttpHeaders().set('Authorization', `Bearer ${token}`);

    this.http.get('http://localhost:8180/auth', { headers, responseType: 'text' })
      .subscribe({
        next: (html) => {
          const newWindow = window.open('', '_blank');
          if (newWindow) {
            newWindow.document.open();
            newWindow.document.write(html);
            newWindow.document.close();
          } else {
            console.error('Falha ao abrir nova aba.');
          }
        },
        error: (err) => {
          console.error('Erro ao acessar BillBridge:', err);
        }
      });
  }

  isOnServicesPage(): boolean {
    return this.currentRoute.endsWith('/services');
  }
}
