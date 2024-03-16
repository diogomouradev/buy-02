import { Component, DestroyRef, inject } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { Router, NavigationExtras } from '@angular/router';
import { UserService } from '../service/user.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { LoginRequest } from '../interfaces/login-request';
import { StateService } from '../service/state.service';
import { User } from '../interfaces/user';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
})
export class LoginComponent {
  formValid: boolean = false;
  user: User | undefined;
  error: string | null = null;

  private userService = inject(UserService);
  private destroyRef = inject(DestroyRef);
  private stateService = inject(StateService);
  private router = inject(Router);

  loginForm: FormGroup = new FormGroup({
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(300),
    ]),
    password: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(30),
    ]),
  });

  goToRegister() {
    this.router.navigate(['register']);
  }

  onSubmit() {
    const request: LoginRequest = this.loginForm.value;
    this.userService.sendLoginRequest(request).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (data) => {
        const navigationExtras: NavigationExtras = { state: { data: data } };
        this.stateService.refreshState(data.jwtToken!, data);
        this.router.navigate(['home'], navigationExtras);
      },
      error: (data) => {
        this.error = data.error.message;
      },
    });
  }

  onValidate() {
    this.formValid = this.loginForm.valid;
  }
}
