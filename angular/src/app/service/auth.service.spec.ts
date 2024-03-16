import { TestBed } from '@angular/core/testing';

import { AuthService } from './auth.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AuthService],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should have the correct address for the HTTP request', () => {
    const address = environment.authCheckURL;
    expect(address).toEqual(environment.authCheckURL);
  });

  it('should make an HTTP request to get user authentication data', () => {
    const testUser = {
      name: 'ali',
      email: 'ali@ali.com',
      id: '123123123123123',
      role: 'SELLER',
    };

    const expectedUrl = environment.authCheckURL;

    service.getAuth().subscribe((user) => {
      expect(user).toEqual(testUser);
    });

    const req = httpMock.expectOne(expectedUrl);
    expect(req.request.method).toBe('GET');

    req.flush(testUser);

    httpMock.verify();
  });
});
