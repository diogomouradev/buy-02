import { TestBed } from '@angular/core/testing';
import { CookieService } from 'ngx-cookie-service';

import { StateService } from './state.service';
import { AuthService } from './auth.service';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

describe('StateService', () => {
  let service: StateService;
  let cookieService: jasmine.SpyObj<CookieService>;
  let router: Router;
  let authService: jasmine.SpyObj<AuthService>;

  beforeEach(() => {
    authService = jasmine.createSpyObj('AuthService', ['getAuth']);
    cookieService = jasmine.createSpyObj('CookieService', ['get', 'delete']);
    router = jasmine.createSpyObj('Router', ['navigate']);

    TestBed.configureTestingModule({
      providers: [
        { provide: AuthService, useValue: authService },
        { provide: AuthService, useValue: authService },
        { provide: CookieService, useValue: cookieService },
        { provide: Router, useValue: router },
      ],
    });
    service = TestBed.inject(StateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should initialize the state when there is a cookie', () => {
    cookieService.get.and.returnValue('example-cookie');
    authService.getAuth.and.returnValue(of({
      name: 'ali',
      email: 'ali@ali.com',
      id: '123123123123123',
      role: 'SELLER',
    }));

    service.ngOnInit();

    service.state?.subscribe((user) => {
      expect(user.name).toBe('ali');
    });
  });

  it('should not initialize the state when there is no cookie', () => {
    cookieService.get.and.returnValue('');

    service.ngOnInit();

    service.state?.subscribe((user) => {
      expect(user.name).toBeFalsy();
    });
  });

  it('should handle getAuth error by deleting the cookie and navigating', () => {
    authService.getAuth.and.returnValue(throwError({ status: 404 }));
    cookieService.get.and.returnValue('example-cookie');

    service.ngOnInit();

    expect(cookieService.delete).toHaveBeenCalledWith('buy-01');
    expect(router.navigate).toHaveBeenCalledWith(['login']);
  });
});
