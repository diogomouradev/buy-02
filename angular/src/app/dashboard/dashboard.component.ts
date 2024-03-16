import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import {
  BehaviorSubject,
  combineLatest,
  debounceTime,
  map,
  Observable,
  of,
} from 'rxjs';
import { CartItem, Order } from '../interfaces/order';
import { User } from '../interfaces/user';
import { OrderService } from '../service/order.service';
import { StateService } from '../service/state.service';
import { AggregatedProduct, Product } from '../interfaces/product';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css'],
})
export class DashboardComponent implements OnInit {
  pendingOrders$: Observable<Order[]> = of([]);
  confirmedOrders$: Observable<Order[]> = of([]);
  cancelledOrders$: Observable<Order[]> = of([]);
  aggregatedProducts$: Observable<AggregatedProduct[]> = of([]);

  cards$: Observable<CartItem[]> = of([]);
  user$: Observable<User> | null = null;
  currentUser: User = {} as User;
  empty: boolean = true;
  confirmedEmpty = false;
  pendingEmpty = false;
  cancelledEmpty = false;
  isSeller: boolean | undefined = undefined;
  confirmedTotalPrice: string = '0';
  cancelledTotalPrice: string = '0';
  pendingTotalPrice: string = '0';
  totalAmount: string = '0';
  filterType: string = 'PENDING';
  searchTerm: string = '';
  searchTermSource = new BehaviorSubject<string>('');
  searchTerm$ = this.searchTermSource.asObservable().pipe(debounceTime(500));

  private stateService = inject(StateService);
  private orderService = inject(OrderService);
  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    this.user$ = this.stateService.getStateAsObservable();
    this.user$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (user) => {
        this.currentUser = user;
        this.isSeller = user.role === 'ROLE_SELLER' || user.role === 'SELLER';
        this.getAllOrders();
      },
    );

    this.orderService.filterType$.pipe(
      takeUntilDestroyed(this.destroyRef),
    )
      .subscribe((filterType) => {
        this.filterType = filterType;
        if (filterType === 'BEST PRODUCTS') {
          this.fetchAndFilterAggregatedProducts();
        } else {
          this.fetchAndFilterOrders();
        }
      });

    this.orderService.orderUpdates$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.getAllOrders();
      });
  }

  private fetchAndFilterAggregatedProducts(): void {
    combineLatest([
      this.orderService.getAggregatedProducts().pipe(
        takeUntilDestroyed(this.destroyRef),
      ),
      this.searchTerm$,
    ]).pipe(
      map(([products, searchTerm]) =>
        this.applySearchFilterToAggregatedProducts(products, searchTerm)
      ),
    ).subscribe((filteredProducts) => {
      this.aggregatedProducts$ = of(this.sortProducts(filteredProducts));
    });
  }

  private fetchAndFilterOrders(): void {
    combineLatest([
      this.orderService.getAllOrders().pipe(
        takeUntilDestroyed(this.destroyRef),
      ),
      this.searchTerm$,
    ]).pipe(
      map(([personalOrder, searchTerm]) => {
        this.confirmedOrders$ = of(
          this.applySearchFilter(personalOrder.confirmed, searchTerm),
        );
        this.pendingOrders$ = of(
          this.applySearchFilter(personalOrder.pending, searchTerm),
        );
        this.cancelledOrders$ = of(
          this.applySearchFilter(personalOrder.cancelled, searchTerm),
        );
      }),
    ).subscribe();
  }

  private applySearchFilterToAggregatedProducts(
    products: AggregatedProduct[],
    searchTerm: string,
  ): AggregatedProduct[] {
    if (!searchTerm) return products;
    return products.filter((product) => {
      return product.product.name.toLowerCase().includes(
        searchTerm.toLowerCase(),
      );
    });
  }

  getAllOrders(): void {
    this.orderService.getAllOrders().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((personalOrder) => {
        this.confirmedOrders$ = of(personalOrder.confirmed);
        this.confirmedEmpty = personalOrder.confirmed.length === 0;
        this.confirmedTotalPrice = this.countTotalValue(
          personalOrder.confirmed,
        );
        this.pendingOrders$ = of(personalOrder.pending);
        this.pendingEmpty = personalOrder.pending.length === 0;
        this.pendingTotalPrice = this.countTotalValue(personalOrder.pending);
        this.totalAmount = this.pendingTotalPrice;
        this.cancelledOrders$ = of(personalOrder.cancelled);
        this.cancelledEmpty = personalOrder.cancelled.length === 0;
        this.cancelledTotalPrice = this.countTotalValue(
          personalOrder.cancelled,
        );
      });
  }

  filterOrders(searchTerm: string): void {
    if (this.filterType !== 'BEST PRODUCTS') {
      this.orderService.getAllOrders().pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe((personalOrder) => {
        this.confirmedOrders$ = of(
          this.applySearchFilter(personalOrder.confirmed, searchTerm),
        );
        this.pendingOrders$ = of(
          this.applySearchFilter(personalOrder.pending, searchTerm),
        );
        this.cancelledOrders$ = of(
          this.applySearchFilter(personalOrder.cancelled, searchTerm),
        );
      });
    } else {
      this.orderService.getAggregatedProducts().pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe((products) => {
        this.aggregatedProducts$ = of(
          this.sortProducts(this.applySearchFilter(products, searchTerm)),
        );
      });
    }
  }

  //eslint-disable-next-line
  private applySearchFilter(orders: any, searchTerm: string): any {
    if (!searchTerm) return orders;
    return orders.filter((order: { product: Product }) => {
      return order.product.name.toLowerCase().includes(
        searchTerm.toLowerCase(),
      );
    });
  }

  private countTotalValue(orders: Order[]) {
    return orders.reduce(
      (acc, order) => acc + order.product.price * order.quantity,
      0,
    ).toFixed(2);
  }

  private sortProducts(products: AggregatedProduct[]) {
    return products.sort((a, b) => b.totalPrice - a.totalPrice);
  }

  changeFilter(filter: string) {
    switch (filter) {
    case 'PENDING': {
      this.totalAmount = this.pendingTotalPrice;
      this.orderService.setFilterType('PENDING');
      break;
    }
    case 'CONFIRMED': {
      this.totalAmount = this.confirmedTotalPrice;
      this.orderService.setFilterType('CONFIRMED');
      break;
    }
    case 'CANCELLED': {
      this.totalAmount = this.cancelledTotalPrice;
      this.orderService.setFilterType('CANCELLED');
      break;
    }
    case 'BEST PRODUCTS': {
      this.totalAmount = this.confirmedTotalPrice;
      this.orderService.setFilterType('BEST PRODUCTS');
      break;
    }
    }
  }

  trackById(_: number, order: Order) {
    return order.id;
  }

  reset() {
    if (this.searchTerm === '') return;
    this.searchTermSource.next('');
    this.searchTerm = '';
    this.filterOrders(this.searchTerm);
  }

  ngOnDestroy(): void {
    this.orderService.resetFilterType();
  }
}
