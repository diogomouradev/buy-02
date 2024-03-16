import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
})
export class AppComponent implements OnInit {
  title = 'buy-01';
  loggedIn = false;

  private router = inject(Router);
  private destroy = inject(DestroyRef);

  ngOnInit(): void {
    this.checkUnauthenticatedRoutes(this.router.url);
    this.router.events.pipe(
      filter((event): event is NavigationEnd => event instanceof NavigationEnd),
    ).pipe(takeUntilDestroyed(this.destroy)).subscribe(
      (event: NavigationEnd) => {
        this.loggedIn = this.checkUnauthenticatedRoutes(
          event.urlAfterRedirects,
        );
      },
    );
  }

  checkUnauthenticatedRoutes(route: string): boolean {
    return !['/login', '/register'].includes(route);
  }
}
