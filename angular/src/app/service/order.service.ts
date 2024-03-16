import { HttpClient, HttpParams } from '@angular/common/http';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { BehaviorSubject, map, Observable, of, Subject, switchMap } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  Cart,
  CartItem,
  CartResponse,
  Order,
  OrderStatusUpdate,
  PersonalOrder,
} from '../interfaces/order';
import { AggregatedProduct, Product } from '../interfaces/product';
import { User } from '../interfaces/user';
import { StateService } from './state.service';

@Injectable({
  providedIn: 'root',
})
export class OrderService {
  private stateService = inject(StateService);
  private destroyRef = inject(DestroyRef);
  private http = inject(HttpClient);

  private user: User = {} as User;

  private filterTypeSubject = new BehaviorSubject<string>('PENDING');
  filterType$ = this.filterTypeSubject.asObservable();

  private orderUpdateSource = new Subject<Order>();
  orderUpdates$ = this.orderUpdateSource.asObservable();

  private cartItemsSource = new Subject<CartItem[]>();
  cartItems$ = this.cartItemsSource.asObservable();

  constructor() {
    this.stateService.getStateAsObservable().pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe((user) => {
      if (!user?.name) return;
      this.user = user;
      this.getCartFromDB().pipe(takeUntilDestroyed(this.destroyRef))
        .subscribe((cart) => {
          this.cartItems$ = of(cart.orders);
        });
    });
  }

  getCartFromDB(): Observable<Cart> {
    const address = environment.cartURL;
    return this.http.get<Cart>(address, { withCredentials: true });
  }

  updateOrders(order: Order): void {
    this.orderUpdateSource.next(order);
  }

  setFilterType(filter: string): void {
    this.filterTypeSubject.next(filter);
  }

  resetFilterType(): void {
    setTimeout(() => this.setFilterType('PENDING'), 15);
  }

  changeOrderStatus(orderId: string, status: string): Observable<Order> {
    const changeStatus = {
      id: orderId,
      status: status,
    } as OrderStatusUpdate;
    const address = environment.ordersURL;
    return this.http.put<Order>(address, changeStatus, {
      withCredentials: true,
    });
  }

  getAggregatedProducts(): Observable<AggregatedProduct[]> {
    return this.getAllOrders().pipe(map((personalOrder) => {
      const orders = personalOrder.confirmed;
      const productMap = new Map<string, AggregatedProduct>();

      orders.forEach((order) => {
        const productId = order.product.id!;
        const existingProduct = productMap.get(productId);

        if (existingProduct) {
          existingProduct.totalQuantity += order.quantity;
          existingProduct.totalPrice += order.product.price * order.quantity;
        } else {
          productMap.set(productId, {
            product: order.product,
            totalQuantity: order.quantity,
            totalPrice: order.product.price * order.quantity,
          });
        }
      });
      return Array.from(productMap.values());
    }));
  }

  placeOrder(): Observable<CartResponse> {
    const address = environment.ordersURL;
    return this.http.post<CartResponse>(address, { withCredentials: true });
  }

  getShoppingCart(filter: string = 'ALL'): Observable<CartItem[]> {
    return this.getCartFromDB().pipe(
      switchMap((cart) => this.filterOrders(filter, cart.orders)),
    );
  }

  filterOrders(
    filter: string,
    cartItems: CartItem[],
  ): Observable<CartItem[]> {
    switch (filter) {
    case 'CONFIRMED':
      return of(
        cartItems.filter((e) => e.status === 'CONFIRMED'),
      );
    case 'PENDING':
      return of(
        cartItems.filter((e) => e.status === 'PENDING'),
      );
    case 'CANCELLED':
      return of(
        cartItems.filter((e) => e.status === 'CANCELLED'),
      );
    case 'ALL':
      return of(cartItems);
    }
    return of([]);
  }

  addToCart(product: Product, quantity: number): void {
    const newOrder = this.createOrder(product, quantity);
    const address = environment.addToCartURL;
    this.http.post<CartItem>(address, newOrder, { withCredentials: true }).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => this.updateOrders({} as Order));
  }

  getAllOrders(): Observable<PersonalOrder> {
    const address = environment.ordersURL;
    return this.http.get<PersonalOrder>(address, { withCredentials: true });
  }

  createOrder(product: Product, quantity: number): CartItem {
    const newOrder = {
      status: 'PENDING',
      buyerId: this.user.id,
      sellerId: product.userId,
      product: product,
      quantity: quantity,
    } as CartItem;
    return newOrder;
  }

  reOrder(orderId: string): void {
    const address = environment.ordersURL;
    const params = new HttpParams().set('reorder', orderId);
    this.http.post<CartResponse>(address, null, {
      params: params,
      withCredentials: true,
    }).pipe(takeUntilDestroyed(this.destroyRef)).subscribe((response) =>
      //NOTE: remove this!
      console.log(response)
    );
  }

  removeItem(id: string): void {
    const address = environment.cartURL + '/' + id;
    this.http.delete<CartItem>(address, { withCredentials: true }).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe(() => {
      this.updateOrders({} as Order);
    });
  }
}
