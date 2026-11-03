import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { API_URL } from './models';
export const authInterceptor: HttpInterceptorFn = (request, next) => {
  const auth = inject(AuthService),
    router = inject(Router);
  const own = request.url.startsWith(API_URL + '/');
  const token = own ? auth.token() : null;
  const outgoing = token
    ? request.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
    : request;
  return next(outgoing).pipe(
    catchError((error: HttpErrorResponse) => {
      if (own && error.status === 401 && !request.url.endsWith('/auth/login')) {
        auth.logout();
        void router.navigate(['/login']);
      }
      return throwError(() => error);
    }),
  );
};
