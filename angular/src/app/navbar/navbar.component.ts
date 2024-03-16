import {
  Component,
  DestroyRef,
  ElementRef,
  inject,
  OnInit,
  ViewChild,
} from '@angular/core';
import { NavigationEnd, Router } from '@angular/router';
import { StateService } from '../service/state.service';
import { UserService } from '../service/user.service';
import { User } from '../interfaces/user';
import { AuthService } from '../service/auth.service';
import { Subject } from 'rxjs';
import { FormStateService } from '../service/form-state.service';
import { filter } from 'rxjs/operators';
import { MediaService } from '../service/media.service';
import { environment } from 'src/environments/environment';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { DataService } from '../service/data.service';
import { OrderService } from '../service/order.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.css'],
})
export class NavbarComponent implements OnInit {
  @ViewChild('navbar')
    navbar: ElementRef | undefined;

  placeholder = environment.placeholder;
  route: string = '';
  seller: boolean = false;
  dash = false;
  home = false;
  profile = false;
  badgeItems: number = 0;
  currentUser: User = {} as User;
  user$ = new Subject<User>();

  private router = inject(Router);
  private userService = inject(UserService);
  private stateService = inject(StateService);
  private formStateService = inject(FormStateService);
  private authService = inject(AuthService);
  private mediaService = inject(MediaService);
  private destroyRef = inject(DestroyRef);
  private dataService = inject(DataService);
  private orderService = inject(OrderService);

  avatar$ = this.mediaService.avatar$;
  dashboard$ = this.dataService.dashboard$;

  ngOnInit(): void {
    if (
      this.router.url === '/profile' || this.router.url === '/shopcart' ||
      this.router.url === '/search'
    ) {
      this.home = false;
      this.dash = false;
      this.profile = true;
    } else if (this.router.url === '/home') {
      this.home = true;
      this.dash = false;
      this.profile = false;
    }

    this.orderService.orderUpdates$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.orderService.getCartFromDB().pipe(
          takeUntilDestroyed(this.destroyRef),
        )
          .subscribe((cart) => {
            this.badgeItems = cart.orders.length;
          });
      });

    this.orderService.getCartFromDB().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((cart) => {
        this.badgeItems = cart.orders.length;
      });

    this.formStateService.formOpen$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((isOpen) => {
        if (isOpen) {
          this.navbar?.nativeElement.classList.add('blur-filter', 'nav-blur');
        } else {
          this.navbar?.nativeElement.classList.remove(
            'blur-filter',
            'nav-blur',
          );
        }
      });

    this.getAuthAndAvatar();

    this.mediaService.imageAdded$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        if (!data) this.mediaService.updateAvatar(this.placeholder);
        else this.getAuthAndAvatar();
      });

    this.userService.usernameAdded$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((data) => {
        this.user$.next(data);
      });

    this.checkRoutes();
  }

  showDashboard(dash: boolean) {
    if (this.router.url !== '/home') {
      this.move('home');
    }

    this.dataService.updateDashboard(dash);
    this.dash = dash;
  }

  private getAuthAndAvatar() {
    this.authService.getAuth().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => {
          this.user$.next(user);
          this.currentUser = user;
          this.seller = this.currentUser.role === 'SELLER';
          if (user.avatar) {
            this.getAvatar();
          } else {
            this.mediaService.updateAvatar(this.placeholder);
          }
        },
      });
  }

  private getAvatar() {
    this.mediaService.getAvatar(
      this.currentUser.id,
    ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (media) => {
        if (media) {
          const image = this.mediaService.formatMedia(media);
          this.mediaService.updateAvatar(image);
        } else {
          this.mediaService.updateAvatar(this.placeholder);
        }
      },
    });
  }

  private checkRoutes() {
    this.router.events
      .pipe(
        filter((event): event is NavigationEnd =>
          event instanceof NavigationEnd
        ),
      ).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((event: NavigationEnd) => {
        if (event?.urlAfterRedirects) {
          this.route = event.urlAfterRedirects;
          this.home = this.route === '/home';
          this.profile = this.route === '/profile';
        }
      });
  }

  move(location: string) {
    this.formStateService.setFormOpen(false);
    this.router.navigate([location]);
  }

  logout() {
    this.userService.logout();
    this.formStateService.setFormOpen(false);
    this.stateService.resetState();
    this.move('login');
  }

  goToProfile() {
    this.formStateService.setFormOpen(false);
    this.move('profile');
  }
}
