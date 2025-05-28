import { inject } from '@angular/core';
import { CanActivateFn, Router, ActivatedRouteSnapshot, RouterStateSnapshot } from '@angular/router';
import { AuthService } from './auth.service';

export const authGuard: CanActivateFn = (
  route: ActivatedRouteSnapshot,
  state: RouterStateSnapshot
) => {
  const router = inject(Router);

  const authService = inject(AuthService);

  const token = localStorage.getItem('accessToken');

  // Check both token and expiry, or fallback to isLoggedin flag
  if (token) {

    const expDate = authService.getExpDateFromToken();

    if (new Date(parseInt(expDate!) * 1000) < new Date()) {
      router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url.split('?')[0] } });
      return false;
    }

    return true;
  }

  router.navigate(['/auth/login'], { queryParams: { returnUrl: state.url.split('?')[0] } });
  return false;
};
