import { ComponentFixture, TestBed } from '@angular/core/testing';
import { LoginComponent } from './login.component';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterTestingModule } from '@angular/router/testing';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { UserService } from '../service/user.service';
import { StateService } from '../service/state.service';
import { of, throwError } from 'rxjs';
import { Router } from '@angular/router';

// Mock services
class MockUserService {
  sendLoginRequest() {
    return of({ jwtToken: 'fake-jwt-token', user: { name: 'Test User' } });
  }
}

class MockStateService {
  refreshState(token: string, user: any) {}
}

describe('LoginComponent', () => {
  let component: LoginComponent;
  let fixture: ComponentFixture<LoginComponent>;
  let userService: UserService;
  let stateService: StateService;
  let router: Router;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ LoginComponent ],
      imports: [ ReactiveFormsModule, FormsModule, RouterTestingModule, HttpClientTestingModule ],
      providers: [
        { provide: UserService, useClass: MockUserService },
        { provide: StateService, useClass: MockStateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    userService = TestBed.inject(UserService);
    stateService = TestBed.inject(StateService);
    router = TestBed.inject(Router);
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('form invalid when empty', () => {
    expect(component.loginForm.valid).toBeFalsy();
  });

  it('name field validity', () => {
    let name = component.loginForm.controls['name'];
    expect(name.valid).toBeFalsy();
    name.setValue("");
    expect(name.hasError('required')).toBeTruthy();
  });

  it('password field validity', () => {
    let password = component.loginForm.controls['password'];
    expect(password.valid).toBeFalsy();
    password.setValue("");
    expect(password.hasError('required')).toBeTruthy();
  });

  it('submitting a form emits a user', () => {
    expect(component.loginForm.valid).toBeFalsy();
    component.loginForm.controls['name'].setValue("tester");
    component.loginForm.controls['password'].setValue("12345678");
    expect(component.loginForm.valid).toBeTruthy();

    let user: any;
    // Mock the sendLoginRequest method to test the response
    spyOn(userService, 'sendLoginRequest').and.returnValue(of({
      name: 'Test User',
      email: 'test@example.com',
      id: '1',
      role: 'CLIENT',
      jwtToken: 'fake-jwt-token', // Assuming jwtToken is part of the User interface
      // Add any other properties that are part of the User interface but optional
    }));
    

    spyOn(stateService, 'refreshState').and.callThrough();
    const navigateSpy = spyOn(router, 'navigate');

    component.onSubmit();

    expect(userService.sendLoginRequest).toHaveBeenCalledWith({name: 'tester', password: '12345678'});
    expect(stateService.refreshState).toHaveBeenCalled();
    expect(navigateSpy).toHaveBeenCalledWith(['home'], jasmine.any(Object)); // Check for navigation with some extras
  });

  it('handle login error', () => {
    const errorResponse = { error: { message: 'Invalid credentials' } };
    spyOn(userService, 'sendLoginRequest').and.returnValue(throwError(() => errorResponse));

    component.loginForm.controls['name'].setValue("wronguser");
    component.loginForm.controls['password'].setValue("wrongpass");
    component.onSubmit();

    expect(component.error).toEqual('Invalid credentials');
  });
});
