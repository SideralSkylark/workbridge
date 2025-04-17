import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { UserService } from '../../../services/user.service';
import { UserResponseDTO } from '../../../models/user-responseDTO.model';

@Component({
  selector: 'app-manage-users',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './manage-users.component.html',
  styleUrls: ['./manage-users.component.scss']
})
export class ManageUsersComponent implements OnInit {
  users: UserResponseDTO[] = [];
  isLoading: boolean = true;

  constructor(private userService: UserService) {}

  ngOnInit(): void {
    this.fetchUsers();
  }

  fetchUsers(): void {
    this.isLoading = true;
    this.userService.getAllUsers().subscribe({
      next: (data) => {
        this.users = data;
        this.isLoading = false;
      },
      error: (err) => {
        console.error('Error fetching users:', err);
        this.isLoading = false;
      }
    });
  }

  getFriendlyRole(role: string): string {
    if (role === 'SERVICE_SEEKER') return 'seeker';
    if (role === 'SERVICE_PROVIDER') return 'service provider';
  
    return role
      .replace('ROLE_', '')
      .toLowerCase()
      .split('_')
      .join(' '); // avoids the regex
  }  

  toggleUserStatus(user: UserResponseDTO): void {
    const action$ = user.enabled
      ? this.userService.disableUser(user.email)
      : this.userService.enableUser(user.email);

    action$.subscribe({
      next: (message) => {
        console.log(message);
        user.enabled = !user.enabled;
      },
      error: (err) => {
        console.error('Failed to update user status:', err);
      }
    });
  }
}