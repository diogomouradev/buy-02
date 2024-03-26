import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { User } from '../interfaces/user';
import { LoginRequest } from '../interfaces/login-request';
import { SignupRequest } from '../interfaces/signup-request';
import { UserUpdateRequest } from '../interfaces/user-update-request';
import { CookieService } from 'ngx-cookie-service';
import { Router } from '@angular/router';

@Injectable({
  providedIn: 'root',
})
export class UserService {
  private usernameAddedSource = new BehaviorSubject<User>({} as User);
  usernameAdded$ = this.usernameAddedSource.asObservable();

  private http = inject(HttpClient);
  private cookieService = inject(CookieService);
  private router = inject(Router);

  updateUsernameAdded(data: User): void {
    this.usernameAddedSource.next(data);
  }

  sendLoginRequest(request: LoginRequest): Observable<User> {
    const address = environment.loginURL;
    return this.http.post<User>(address, request, { withCredentials: true });
  }

  sendSignupRequest(request: SignupRequest): Observable<User> {
    const address = environment.signupURL;
    return this.http.post<User>(address, request, { withCredentials: true });
  }

  sendUserInfoRequest(): Observable<User[]> {
    const address = environment.usersURL;
    return this.http.get<User[]>(address, { withCredentials: true });
  }

  getUserInfo(): Observable<User[]> {
    const address = environment.usersURL;
    return this.http.get<User[]>(address, { withCredentials: true });
  }

  getOwnerInfo(id: string): Observable<User> {
    const address = environment.usersURL + '/' + id;
    return this.http.get<User>(address, { withCredentials: true });
  }

  logout() {
    this.cookieService.delete('buy-01');
    this.cookieService.deleteAll();
    const expirationDate = new Date('Thu, 01 Jan 1970 00:00:00 UTC');
    this.cookieService.set(
      'buy-01',
      '',
      expirationDate,
      '/',
      'thewarehouse.rocks',
      true,
      'Lax',
    );
    this.router.navigate(['/login']);
  }

  updateUser(request: UserUpdateRequest, id: string): Observable<User> {
    const address = environment.usersURL + '/' + id;
    return this.http.put<User>(address, request, { withCredentials: true });
  }

  //eslint-disable-next-line
  deleteUser(userId: string): Observable<any> {
    const address = environment.usersURL + '/' + userId;
    return this.http.delete(address, { withCredentials: true });
  }
}
