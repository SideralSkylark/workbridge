import { Component } from '@angular/core';
import { Router, NavigationEnd, RouterOutlet } from '@angular/router';
import { filter } from 'rxjs';

@Component({
  selector: 'app-admin-panel',
  imports: [RouterOutlet],
  standalone: true,
  templateUrl: './admin.component.html',
  styleUrl: './admin.component.scss'
})
export class AdminPanelComponent {
  currentRoute: string = '';

  constructor(private router: Router) {
    this.currentRoute = this.router.url;

    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe((event: NavigationEnd) => {
      this.currentRoute = event.urlAfterRedirects;
    });
  }

  goToManageUsers(): void {
    this.router.navigate(['dashboard', 'admin', 'manage-users']);
  }

  goToApproveProviders(): void {
    this.router.navigate(['dashboard', 'admin', 'approve-providers']);
  }
}
