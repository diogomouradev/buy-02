import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { User } from '../interfaces/user';
import { AuthService } from '../service/auth.service';
import { CookieService } from 'ngx-cookie-service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

export const authGuard: CanActivateFn = (route) => {
  const path = route.routeConfig?.path;

  const authService = inject(AuthService);
  const router = inject(Router);
  const cookieService = inject(CookieService);

  authService.getAuth().pipe(takeUntilDestroyed()).subscribe({
    next: (user: User) => {
      switch (path) {
      case 'register':
      case 'login': {
        if (!user || !user.name) { //NOSONAR
          return true;
        } else {
          router.navigate(['home']);
          return false;
        }
      }
      case 'search':
      case 'shopcart':
      case 'dashboard':
      case 'profile':
      case 'home': {
        if (user?.name) {
          return true;
        } else {
          router.navigate(['login']);
          return false;
        }
      }
      }
      return false;
    },
    error: () => {
      cookieService.delete('buy-01');
    },
  });

  return true;
};
