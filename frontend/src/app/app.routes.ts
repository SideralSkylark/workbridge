import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { VerifyComponent } from './features/auth/verify/verify.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { AuthGuard } from './core/guards/auth.guard';
import { RoleGuard } from './core/guards/role.guard';
import { AdminPanelComponent } from './features/dashboard/admin/admin.component';
import { ManageServicesComponent } from './features/dashboard/provider/provider.component';
import { ServiceRequestsComponent } from './features/dashboard/seeker/seeker.component';
import { ChatComponent } from './features/dashboard/chat/chat.component';
import { ServicesComponent } from './features/dashboard/provider/my-services/my-services.component';
import { BookedServicesComponent } from './features/dashboard/provider/booked-services/booked-services.component';
import { FeedComponent } from './features/dashboard/seeker/feed/feed.component';
import { MyBookingsComponent } from './features/dashboard/seeker/my-bookings/my-bookings.component';
import { ManageUsersComponent } from './features/dashboard/admin/manage-users/manage-users.component';
import { AproveProvidersComponent } from './features/dashboard/admin/aprove-providers/aprove-providers.component';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    { path: 'verify', component: VerifyComponent },
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [AuthGuard],
        children: [
            {
                path: 'admin',
                component: AdminPanelComponent,
                canActivate: [RoleGuard],
                data: { roles: ['ADMIN'] },
                children: [
                    { path: 'manage-users', component: ManageUsersComponent },
                    { path: 'approve-providers', component: AproveProvidersComponent },
                    { path: '', redirectTo: 'manage-users', pathMatch: 'full' }
                ]
            },
            {
                path: 'requests',
                component: ServiceRequestsComponent,
                canActivate: [RoleGuard],
                data: { roles: ['SERVICE_SEEKER'] },
                children: [
                    { path: 'feed', component: FeedComponent },
                    { path: 'bookings', component: MyBookingsComponent },
                    { path: '', redirectTo: 'feed', pathMatch: 'full' }
                ]
            },
            {
                path: 'manage',
                component: ManageServicesComponent,
                canActivate: [RoleGuard],
                data: { roles: ['SERVICE_PROVIDER'] },
                children: [
                    { path: 'services', component: ServicesComponent },
                    { path: 'bookings', component: BookedServicesComponent },
                    { path: '', redirectTo: 'services', pathMatch: 'full' }
                ] },

            { path: 'chat', component: ChatComponent, canActivate: [RoleGuard], data: {roles: ['ADMIN', 'SERVICE_SEEKER', 'SERVICE_PROVIDER']}},
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' }
        ]
    },
    { path: '**', redirectTo: '/login' },
];
