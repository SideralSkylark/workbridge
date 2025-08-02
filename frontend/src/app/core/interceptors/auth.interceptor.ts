import { Injectable } from '@angular/core';
import { HttpRequest, HttpHandler, HttpEvent, HttpInterceptor } from '@angular/common/http';
import { catchError, Observable, throwError } from 'rxjs';
import { AuthService } from '../../features/auth/auth.service';

@Injectable()
export class AuthInterceptor implements HttpInterceptor {
    constructor(private authService: AuthService) { }

    intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const modifiedRequest = request.clone({
          withCredentials: true
        });

        return next.handle(modifiedRequest).pipe(
          catchError(error => {
            if (error.status === 401) {
              // Maybe redirect to login or notify user
            }
            return throwError(error);
          })
        );
      }
}
