import { Routes } from '@angular/router';
import { LoginComponent } from './auth/login/login.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { AuthGuard } from './auth/auth.guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent},
    { path: '', redirectTo: '/login', pathMatch: 'full' },
    { path: 'dashboard', component: DashboardComponent, canActivate: [AuthGuard] },
    { path: '**', redirectTo: '/login' },
    // nao ponhas rotas abaixo desta utlima, poe antes dela.
];
