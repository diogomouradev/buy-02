import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { OrderService } from '../service/order.service';
import { StateService } from '../service/state.service';
import { User } from '../interfaces/user';
import { CartItem, Order } from '../interfaces/order';

@Component({
  selector: 'app-shopping-cart',
  templateUrl: './shopping-cart.component.html',
  styleUrls: ['./shopping-cart.component.css'],
})
export class ShoppingCartComponent implements OnInit {
  cartItems$: Observable<CartItem[]> = of([]);
  errorMessages: Set<string> = new Set();
  problematicOrderIds: Set<string> = new Set();
  currentUser: User = {} as User; //An observable for the current user's data, fetched from stateService.
  user$: Observable<User> | null = null;
  empty = true;

  private destroyRef = inject(DestroyRef);
  private orderService = inject(OrderService);
  private stateService = inject(StateService);

  ngOnInit(): void {
    this.user$ = this.stateService.getStateAsObservable();
    this.user$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (user) => {
        this.currentUser = user;
        this.getOrders();
      },
    );

    this.orderService.orderUpdates$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.getOrders();
      });
  }

  getOrders(): void {
    this.orderService.getShoppingCart().pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: (products) => {
        this.cartItems$ = of(products);
        this.empty = products.length === 0;
      },
    });
  }

  confirmOrder(): void {
    this.orderService.placeOrder().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((response) => {
        if (!response.processed) {
          this.errorMessages = new Set(response?.orderModifications?.notes);
          this.problematicOrderIds = new Set(
            response.orderModifications?.modifications?.map((e) => e.id!),
          );
        }
        this.cartItems$ = of(response.cart.orders);
        this.orderService.updateOrders({} as Order);
      });
  }

  isProblematicOrder(orderId: string): boolean {
    return this.problematicOrderIds.has(orderId);
  }
}
