import { fakeAsync, TestBed, tick } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { UserService } from './user.service';
import { environment } from 'src/environments/environment';
import { LoginRequest } from '../interfaces/login-request';
import { SignupRequest } from '../interfaces/signup-request';
import { User } from '../interfaces/user';
import { UserUpdateRequest } from '../interfaces/user-update-request';
import { Router } from '@angular/router';
import { CookieService } from 'ngx-cookie-service';

describe('UserService', () => {
  let service: UserService;
  let httpMock: HttpTestingController;
  let router: Router;
  let cookieService: CookieService;

  const mockUsers = [
    {
      id: '123123123123',
      email: 'test@test.com',
      name: 'ali',
      role: 'ROLE_CLIENT',
    },
    {
      name: 'ali',
      email: 'ali@ali.com',
      role: 'SELLER',
      id: '123123123',
    },
  ];

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [UserService],
    });
    service = TestBed.inject(UserService);
    httpMock = TestBed.inject(HttpTestingController);
    router = TestBed.inject(Router);
    cookieService = TestBed.inject(CookieService);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send a login request', () => {
    const loginRequest: LoginRequest = {
      name: 'ali',
      password: 'test123',
    };
    const user: User = {
      id: '123123123123',
      email: 'test@test.com',
      jwtToken: 'ajsdklajsdlskldjaskdjalksdjlaksjdlkasjdlkajsdlslkajsd',
      name: 'ali',
      role: 'ROLE_CLIENT',
    };

    const expectedResponse = user;

    service.sendLoginRequest(loginRequest).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(environment.loginURL);
    expect(req.request.method).toBe('POST');
    req.flush(expectedResponse);
  });

  it('should send a signup request', () => {
    const signupRequest: SignupRequest = {
      name: 'ali',
      email: 'ali@ali.com',
      password: 'test123',
      confirmPassword: 'test123',
      role: 'SELLER',
    };
    const expectedResponse = {
      name: 'ali',
      email: 'ali@ali.com',
      confirmPassword: 'test123',
      role: 'SELLER',
      id: '123123123',
    } as User;

    service.sendSignupRequest(signupRequest).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(environment.signupURL);
    expect(req.request.method).toBe('POST');
    req.flush(expectedResponse);
  });

  it('should get user info', () => {
    const expectedResponse: User[] = mockUsers;
    service.getUserInfo().subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(environment.usersURL);
    expect(req.request.method).toBe('GET');
    req.flush(expectedResponse);
  });

  it(
    'should delete cookie and log out',
    fakeAsync(() => {
      const routerSpy = spyOn(router, 'navigate');
      const cookieSpy = spyOn(cookieService, 'deleteAll');

      service.logout();
      tick();
      expect(routerSpy).toHaveBeenCalledWith(['/login']);
      expect(cookieSpy).toHaveBeenCalled();
    }),
  );

  it('should send a user info request', () => {
    const expectedResponse: User[] = mockUsers;

    service.sendUserInfoRequest().subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(environment.usersURL);
    expect(req.request.method).toBe('GET');
    req.flush(expectedResponse);
  });

  it('should get owner info', () => {
    const ownerId = '123456789';
    const expectedResponse: User = mockUsers[1];

    service.getOwnerInfo(ownerId).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(`${environment.usersURL}/${ownerId}`);
    expect(req.request.method).toBe('GET');
    req.flush(expectedResponse);
  });

  it('should update a user', () => {
    const userId = '123456789';
    const updateUserRequest: UserUpdateRequest = mockUsers[1];
    const expectedResponse: User = mockUsers[1];

    service.updateUser(updateUserRequest, userId).subscribe((response) => {
      expect(response).toEqual(expectedResponse);
    });

    const req = httpMock.expectOne(`${environment.usersURL}/${userId}`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(updateUserRequest);
    req.flush(expectedResponse);
  });

  it('should delete user', () => {
    const mockResponse = { message: 'User deleted successfully' };
    const mockId = '123';
    service.deleteUser(mockId).subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpMock.expectOne(`${environment.usersURL}/${mockId}`);
    expect(req.request.method).toBe('DELETE');
    req.flush(mockResponse);
  });

  it('signup url should be environment.signupURL', () => {
    expect(environment.signupURL).toEqual(
      'https://localhost:443/api/auth/signup',
    );
  });

  it('signup url should be environment.loginURL', () => {
    expect(environment.loginURL).toEqual(
      'https://localhost:443/api/auth/signin',
    );
  });
});
