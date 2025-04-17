import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { RegisterComponent } from './auth/register/register.component';
import { VerifyComponent } from './auth/verify/verify.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './auth/auth.guard';
import { RoleGuard } from './auth/role.guard';
import { AdminPanelComponent } from './dashboard/admin-panel/admin-panel.component';
import { ManageServicesComponent } from './dashboard/manage-services/manage-services.component';
import { ServiceRequestsComponent } from './dashboard/service-requests/service-requests.component';
// import { ChatComponent } from './dashboard/chat/chat.component';
import { ServicesComponent } from './dashboard/manage-services/services/services.component';
import { BookedServicesComponent } from './dashboard/manage-services/booked-services/booked-services.component';
import { FeedComponent } from './dashboard/service-requests/feed/feed.component';
import { MyBookingsComponent } from './dashboard/service-requests/my-bookings/my-bookings.component';

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
            { path: 'admin', component: AdminPanelComponent, canActivate: [RoleGuard], data: { roles: ['ADMIN'] } },
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
            // { path: 'chat', component: ChatComponent, canActivate: [RoleGuard], data: {roles: ['ADMIN', 'SERVICE_SEEKER', 'SERVICE_PROVIDER']}},
            { path: '', redirectTo: 'dashboard', pathMatch: 'full' } 
        ]
    },
    { path: '**', redirectTo: '/login' },
    // nao ponhas rotas abaixo desta utlima, poe antes dela.
];
