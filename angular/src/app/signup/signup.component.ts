import { Component, DestroyRef, inject } from '@angular/core';
import { NavigationExtras, Router } from '@angular/router';
import {
  AbstractControl,
  FormControl,
  FormGroup,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';
import { UserService } from '../service/user.service';
import { SignupRequest } from '../interfaces/signup-request';
import { StateService } from '../service/state.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'signup',
  templateUrl: './signup.component.html',
})
export class SignupComponent {
  private checkPassword: ValidatorFn = (
    form: AbstractControl,
  ): ValidationErrors | null => {
    return form.get('password')?.value === form.get('confirmPassword')?.value
      ? null
      : { notSame: true };
  };
  formValid: boolean = false;
  error: string | null = null;
  registerForm: FormGroup = new FormGroup(
    {
      name: new FormControl('', [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(30),
      ]),
      email: new FormControl('', [
        Validators.required,
        Validators.minLength(3),
        Validators.maxLength(300),
        Validators.email,
      ]),
      password: new FormControl('', [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(30),
      ]),
      confirmPassword: new FormControl('', [
        Validators.required,
        Validators.minLength(4),
        Validators.maxLength(30),
      ]),
      role: new FormControl(false),
    },
    { validators: this.checkPassword },
  );

  private stateService = inject(StateService);
  private userService = inject(UserService);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);

  onValidate() {
    this.formValid = this.registerForm.valid;
  }

  goToLogin() {
    this.router.navigate(['login']);
  }

  onSubmit() {
    this.formValid = false;
    const request: SignupRequest = this.registerForm.value;
    if (request.role) {
      request.role = 'SELLER';
    } else {
      request.role = 'CLIENT';
    }
    this.userService.sendSignupRequest(request).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      error: (error) => {
        this.error = error.error.message;
      },
      complete: () => {
        this.autoLogin(request);
      },
    });
  }

  autoLogin(request: SignupRequest): void {
    this.userService.sendLoginRequest(request).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (data) => {
        const navigationExtras: NavigationExtras = { state: { data: data } };
        this.stateService.refreshState(data.jwtToken!, data);
        this.router.navigate(['home'], navigationExtras);
      },
      error: (data) => console.log(data),
    });
  }
}
