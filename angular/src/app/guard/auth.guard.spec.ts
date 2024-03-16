import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import {
  ActivatedRouteSnapshot,
  CanActivateFn,
  Router,
  RouterStateSnapshot,
} from '@angular/router';

import { authGuard } from './auth.guard';
import { of } from 'rxjs';
import { User } from '../interfaces/user';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { AuthService } from '../service/auth.service';
import { CookieService } from 'ngx-cookie-service';
import { inject } from '@angular/core';

describe('authGuard', () => {
  const executeGuard: CanActivateFn = (
    route: ActivatedRouteSnapshot,
    state: RouterStateSnapshot,
  ) => authGuard(route, state);

  const cookieServiceSpy = jasmine.createSpyObj('CookieService', ['delete']);
  const routerSpy = jasmine.createSpyObj('Router', ['navigate']);

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [
        AuthService,
        { provide: CookieService, useValue: cookieServiceSpy },
        { provide: Router, useValue: routerSpy },
      ],
      imports: [HttpClientTestingModule],
    });
  });

  it('should be created', () => {
    expect(executeGuard).toBeTruthy();
  });

  it('should return true user is authenticated and path is "dashboard"', () => {
    TestBed.runInInjectionContext(() => {
      const route = {
        routeConfig: { path: 'dashboard' },
      } as ActivatedRouteSnapshot;
      const authService = inject(AuthService);
      const authSpy = spyOn(authService, 'getAuth').and.returnValue(of({
        name: 'testUser',
        email: 'ali@gmail.com',
        id: '123123123',
        role: 'SELLER',
      } as User));
      //eslint-disable-next-line @typescript-eslint/no-explicit-any
      const canActivate = executeGuard(route, null as any);
      expect(authSpy).toHaveBeenCalled();
      expect(canActivate).toBe(true);
    });
  });

  it(
    'should return true and redirect to home if user is authenticated and path is "login"',
    fakeAsync(() => {
      TestBed.runInInjectionContext(() => {
        const route = {
          routeConfig: { path: 'login' },
        } as ActivatedRouteSnapshot;
        const authService = inject(AuthService);
        const authSpy = spyOn(authService, 'getAuth').and.returnValue(of({
          name: 'testUser',
          email: 'ali@gmail.com',
          id: '123123123',
          role: 'SELLER',
        } as User));
        //eslint-disable-next-line @typescript-eslint/no-explicit-any
        const canActivate = executeGuard(route, null as any);
        expect(authSpy).toHaveBeenCalled();
        expect(canActivate).toBe(true);
        tick();
        expect(routerSpy.navigate).toHaveBeenCalledWith(['home']);
      });
    }),
  );
});
