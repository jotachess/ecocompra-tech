import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';
export const adminGuard: CanActivateFn = () => {
  const auth = inject(AuthService), router = inject(Router);
  return auth.valid() ? (auth.admin() || router.createUrlTree(['/inicio'])) : router.createUrlTree(['/login']);
};
