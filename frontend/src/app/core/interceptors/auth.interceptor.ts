import { Injectable } from '@angular/core';
import {
  HttpRequest, HttpHandler, HttpEvent, HttpInterceptor, HttpErrorResponse
} from '@angular/common/http';
import { catchError, switchMap, Observable, throwError, of} from 'rxjs';
import { AuthService } from '../../features/auth/auth.service';
import { Router } from '@angular/router';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
  private isRefreshing: boolean = false;
  private failedRequestsQueue: HttpRequest<any>[] = [];

  constructor(private authService: AuthService, private router: Router) { }

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    const requestWithCredentials = req.clone({ withCredentials: true });

    return next.handle(requestWithCredentials).pipe(
      catchError((error: HttpErrorResponse) => {
        if (
          error.status === 401 &&
          !req.url.includes('/refresh-token') &&
          !this.isRefreshing
        ) {
          this.isRefreshing = true;

          return this.authService.refreshToken().pipe(
            switchMap(() => {
              this.isRefreshing = false;
              const retriedRequest = req.clone({ withCredentials: true });
              return next.handle(retriedRequest);
            }),
            catchError(refreshError => {
              this.isRefreshing = false;
              this.authService.logout;
              this.router.navigate(['/login']);
              return throwError(() => refreshError);
            })
          );
        }

        return throwError(() => error);
      })
    );
  }
}
