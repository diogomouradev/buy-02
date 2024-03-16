import { TestBed } from '@angular/core/testing';
import { ValidatorService } from './validator.service';
import { FormControl, FormGroup } from '@angular/forms';

describe('ValidatorService', () => {
  let service: ValidatorService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ValidatorService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should validate a number correctly', () => {
    const validator = service.numberValidator();
    const control = new FormControl('123');
    expect(validator(control)).toBeNull();
  });

  it('should report an invalid number', () => {
    const validator = service.numberValidator();
    const control = new FormControl('abc');
    expect(validator(control)).toEqual({ 'invalidNumber': { value: 'abc' } });
  });

  it('should validate a matching password correctly', () => {
    const validator = service.passwordValidator();
    const form = new FormGroup({
      password: new FormControl('password123'),
      confirmPassword: new FormControl('password123'),
    });
    expect(validator(form)).toBeNull();
  });

  it('should report an invalid password', () => {
    const validator = service.passwordValidator();
    const form = new FormGroup({
      password: new FormControl('password123'),
      confirmPassword: new FormControl('password456'),
    });
    expect(validator(form)).toEqual({ 'invalidPassword': true });
  });
});
