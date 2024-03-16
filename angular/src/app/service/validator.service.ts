import { Injectable } from '@angular/core';
import {
  AbstractControl,
  ValidationErrors,
  ValidatorFn,
  Validators,
} from '@angular/forms';

@Injectable({
  providedIn: 'root',
})
export class ValidatorService {
  numberValidator(): ValidatorFn {
    //eslint-disable-next-line
    return (control: AbstractControl): { [key: string]: any } | null => {
      const value = control.value;
      if (value && isNaN(Number(value))) {
        return { 'invalidNumber': { value: control.value } };
      }
      return null;
    };
  }

  passwordValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const passwordsMatch = control.get('password')?.value ===
        control?.get('confirmPassword')?.value;
      const confirmPassword = control.get('confirmPassword')?.value;
      const password = control.value;
      if (!password && !confirmPassword) return null;

      return Validators.minLength(4)(control) ||
          Validators.maxLength(30)(control) ||
          !passwordsMatch
        ? { invalidPassword: true }
        : null;
    };
  }
  usernameValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const username = control.value;
      if (!username) return null;

      return Validators.minLength(4)(control) ||
          Validators.maxLength(30)(control)
        ? { invalidName: true }
        : null;
    };
  }
  emailValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const email = control.value;
      if (!email) return null;

      return Validators.minLength(4)(control) ||
          Validators.maxLength(30)(control) ||
          Validators.email(control)
        ? { invalidEmail: true }
        : null;
    };
  }

  productNameValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const name = control.value;
      if (!name) return null;

      return Validators.minLength(4)(control) ||
          Validators.maxLength(50)(control)
        ? { nameInvalid: true }
        : null;
    };
  }

  productDescriptionValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const description = control.value;
      if (!description) return null;

      return Validators.minLength(4)(control) ||
          Validators.maxLength(300)(control)
        ? { descriptionInvalid: true }
        : null;
    };
  }

  productPriceValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const price = control.value;
      if (!price) return null;

      return Validators.min(0)(control) ||
          Validators.max(9999999999)(control)
        ? { priceInvalid: true }
        : null;
    };
  }

  productQuantityValidator(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const quantity = control.value;
      if (!quantity) return null;

      return Validators.min(0)(control) ||
          Validators.max(99999)(control)
        ? { quantityInvalid: true }
        : null;
    };
  }
}
