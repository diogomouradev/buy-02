import { ComponentFixture, TestBed } from '@angular/core/testing';
import { NavbarComponent } from './navbar.component';
import { AuthService } from '../service/auth.service';
import { of } from 'rxjs';
import { StateService } from '../service/state.service';
import { UserService } from '../service/user.service';
import { NavigationEnd, Router } from '@angular/router';
import { MatIconModule } from '@angular/material/icon';
import { User } from '../interfaces/user';
import { HttpClientModule } from '@angular/common/http';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatBadgeModule } from '@angular/material/badge';

describe('NavbarComponent', () => {
  let component: NavbarComponent;
  let fixture: ComponentFixture<NavbarComponent>;
  let userService: UserService;
  let stateService: StateService;
  let router: Router;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [NavbarComponent],
      imports: [
        MatIconModule,
        HttpClientModule,
        HttpClientTestingModule,
        MatBadgeModule,
      ],
      providers: [
        {
          provide: Router,
          useValue: {
            events: of(new NavigationEnd(0, '/home', '/home')),
            navigate: jasmine.createSpy('navigate'),
          },
        },
        {
          provide: UserService,
          useValue: {
            logout: jasmine.createSpy('logout'),
            usernameAdded$: of('ali'),
          },
        },
        {
          provide: StateService,
          useValue: {
            resetState: jasmine.createSpy('resetState'),
            getStateAsObservable: () =>
              of({
                name: 'ali',
                email: 'ali@ali.com',
                id: '123123123123123',
                role: 'SELLER',
              } as User),
          },
        },
        {
          provide: AuthService,
          useValue: {
            getAuth: () =>
              of({
                name: 'ali',
                email: 'ali@ali.com',
                id: '123123123123123',
                role: 'SELLER',
              } as User),
          },
        },
      ],
    });
    fixture = TestBed.createComponent(NavbarComponent);
    component = fixture.componentInstance;
    userService = TestBed.inject(UserService);
    stateService = TestBed.inject(StateService);
    router = TestBed.inject(Router);
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call logout when logging out', () => {
    component.logout();
    expect(userService.logout).toHaveBeenCalled();
  });

  it('should call resetState when logging out', () => {
    component.logout();
    expect(stateService.resetState).toHaveBeenCalled();
  });

  it('should call navigate when moving', () => {
    component.move('login');
    expect(router.navigate).toHaveBeenCalledWith(['login']);
  });
});
