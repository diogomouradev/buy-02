import { HttpClient } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { User } from '../interfaces/user';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private http = inject(HttpClient);

  getAuth(): Observable<User> {
    const address = environment.authCheckURL;
    return this.http.get<User>(address, { withCredentials: true });
  }
}
