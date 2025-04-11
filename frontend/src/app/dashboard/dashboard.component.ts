import { Component, OnInit } from '@angular/core';
import { AuthService } from '../auth/auth.service';
import { MenuItem } from '../models/menu-item.model';
import { RouterLink, RouterOutlet } from '@angular/router';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, RouterOutlet, CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
  standalone: true,
})
export class DashboardComponent implements OnInit {
  userRoles: string[] = [];
  isSidebarOpen = false;

  menuItems: MenuItem[] = [
    { label: 'Admin Panel', icon: 'bi bi-shield-lock', route: '/dashboard/admin', roles: ['ADMIN'] },
    { label: 'Service Requests', icon: 'bi bi-box', route: '/dashboard/requests', roles: ['SERVICE_SEEKER'] },
    { label: 'Manage Services', icon: 'bi bi-gear', route: '/dashboard/manage', roles: ['SERVICE_PROVIDER'] },
    { label: 'Chat', icon: 'bi bi-chat-dots', route: '/dashboard/chat', roles: ['ADMIN', 'SERVICE_SEEKER', 'SERVICE_PROVIDER'] },
  ];

  constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.userRoles = this.authService.getUserRoles();
  }

  get visibleMenuItems(): MenuItem[] {
    return this.menuItems.filter(item =>
      item.roles.some(role => this.userRoles.includes(role))
    );
  }

  toggleSidebar(): void {
    this.isSidebarOpen = !this.isSidebarOpen;
  }
}