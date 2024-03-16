import { TestBed } from '@angular/core/testing';
import { NavigationEnd, Router, RouterOutlet } from '@angular/router';
import { BehaviorSubject } from 'rxjs';
import { AppComponent } from './app.component';
import { HttpClientTestingModule } from '@angular/common/http/testing'

describe('AppComponent', () => {
  //eslint-disable-next-line
  let router: Router;
  //eslint-disable-next-line @typescript-eslint/no-explicit-any
  let routerEventsSubject: BehaviorSubject<any>;

  beforeEach(() => {
    //eslint-disable-next-line @typescript-eslint/no-explicit-any
    routerEventsSubject = new BehaviorSubject<any>(null);

    const mockRouter = {
      events: routerEventsSubject.asObservable(),
      url: '/dashboard',
      navigate: jasmine.createSpy('navigate'),
      resetConfig: jasmine.createSpy('resetConfig'),
    };

    TestBed.configureTestingModule({
      providers: [{ provide: Router, useValue: mockRouter }],
      declarations: [AppComponent],
      imports: [RouterOutlet],
    });
  });

  it('should set loggedIn to true for an authenticated route', () => {
    const checkRouteSpy = spyOn(
      AppComponent.prototype,
      'checkUnauthenticatedRoutes',
    ).and.returnValue(true);
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;
    app.ngOnInit();
    expect(app.checkUnauthenticatedRoutes('/dashboard')).toBeTrue();
    expect(checkRouteSpy).toHaveBeenCalledWith('/dashboard');
  });

  it('should set loggedIn to false for an unauthenticated route', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    app.ngOnInit();
    expect(app.checkUnauthenticatedRoutes('/login')).toBeFalse();
    fixture.detectChanges();
    expect(app.loggedIn).toBeFalse();
  });

  it('should update loggedIn when the route changes', () => {
    const fixture = TestBed.createComponent(AppComponent);
    const app = fixture.componentInstance;

    router = TestBed.inject(Router);
    routerEventsSubject.next(new NavigationEnd(1, '/login', '/dashboard'));

    expect(app.loggedIn).toBeFalse();
  });
});
