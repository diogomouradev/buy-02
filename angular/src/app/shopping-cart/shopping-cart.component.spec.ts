import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ShoppingCartComponent } from './shopping-cart.component';
import { OrderCardComponent } from '../order-card/order-card.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { of, Subject } from 'rxjs';
import {
  Cart,
  CartItem,
  CartResponse,
  Order,
  OrderModifications,
} from '../interfaces/order';
import { OrderService } from '../service/order.service';
import { StateService } from '../service/state.service';
import { Product } from '../interfaces/product';

describe('ShoppingCartComponent', () => {
  let component: ShoppingCartComponent;
  let fixture: ComponentFixture<ShoppingCartComponent>;
  //eslint-disable-next-line
  let orderServiceMock: any;
  //eslint-disable-next-line
  let stateServiceMock: any;
  //eslint-disable-next-line
  let mockUser: any;
  let mockCart: Cart;
  let mockCartItems: CartItem[];
  let mockProducts: Product[];
  let mockCartResponse: CartResponse;
  let mockOrderModifications: OrderModifications;
  let mockFailNotes: Set<string>;

  beforeEach(() => {
    mockUser = {
      name: 'test',
      email: 'test@test.com',
      id: 'testId',
      role: 'SELLER',
    };

    mockProducts = [
      {
        name: 'test123',
        quantity: 5,
        description: 'test desc',
        price: 12.99,
        userId: 'seller1',
        id: '111',
        thumbnail: 'placeholder',
      },
      {
        name: 'test',
        quantity: 6,
        description: 'test desc2',
        price: 99.99,
        userId: 'seller2',
        id: '222',
        thumbnail: 'placeholder',
      },
      {
        name: 'third product',
        quantity: 2,
        description: 'third description',
        price: 299.99,
        userId: 'seller3',
        id: '333',
        thumbnail: 'placeholder',
      },
    ];

    mockCartItems = [
      {
        id: '1',
        status: 'PENDING',
        buyerId: 'buyer1',
        sellerId: 'seller1',
        product: mockProducts[0],
        quantity: 1,
      },
      {
        id: '2',
        status: 'CANCELLED',
        buyerId: 'buyer1',
        sellerId: 'seller2',
        product: mockProducts[1],
        quantity: 2,
      },
      {
        id: '3',
        status: 'CONFIRMED',
        buyerId: 'buyer1',
        sellerId: 'seller3',
        product: mockProducts[2],
        quantity: 3,
      },
    ];

    mockCart = {
      orders: mockCartItems,
    };

    mockFailNotes = new Set<string>();
    mockFailNotes.add('test note');
    mockFailNotes.add('test note2');

    mockOrderModifications = {
      notes: mockFailNotes,
      modifications: [
        mockCartItems[0] as Order,
        mockCartItems[1] as Order,
      ],
    };

    mockCartResponse = {
      cart: mockCart,
      processed: false,
      orderModifications: mockOrderModifications,
    };

    orderServiceMock = {
      getShoppingCart: jasmine.createSpy().and.returnValue(of(mockCartItems)),
      placeOrder: jasmine.createSpy().and.returnValue(of(mockCartResponse)),
      orderUpdateSource: new Subject<Order>(),
      orderUpdates$: of({} as Order),
      updateOrders: () => {
        orderServiceMock.orderUpdateSource.next({} as Order);
      },
    };
    orderServiceMock.orderUpdates$ = orderServiceMock.orderUpdateSource
      .asObservable();

    stateServiceMock = {
      getStateAsObservable: jasmine.createSpy().and.returnValue(of(mockUser)),
    };

    TestBed.configureTestingModule({
      declarations: [ShoppingCartComponent, OrderCardComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: OrderService, useValue: orderServiceMock },
        { provide: StateService, useValue: stateServiceMock },
      ],
    });
    fixture = TestBed.createComponent(ShoppingCartComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(
    'should initialize the current user and get the orders',
    () => {
      const getOrderSpy = spyOn(component, 'getOrders');
      component.ngOnInit();
      expect(stateServiceMock.getStateAsObservable).toHaveBeenCalled();
      expect(component.currentUser).toEqual(mockUser);
      expect(getOrderSpy).toHaveBeenCalled();
    },
  );

  it('should display cart items', () => {
    component.cartItems$.subscribe((cartItems) => {
      expect(cartItems).toEqual(mockCartItems);
      expect(component.empty).toBeFalse();
    });
  });

  it('should handle empty cart', () => {
    orderServiceMock.getShoppingCart.and.returnValue(of([]));
    component.ngOnInit();
    fixture.detectChanges();
    component.cartItems$.subscribe((items) => {
      expect(items.length).toBe(0);
      expect(component.empty).toBeTrue();
    });
  });

  it('should handle confirming of order, with no modifications', () => {
    const mockOrderResponse = {
      processed: true,
      cart: { orders: [] },
      orderModifications: null,
    };
    orderServiceMock.placeOrder.and.returnValue(of(mockOrderResponse));

    component.confirmOrder();
    fixture.detectChanges();

    expect(orderServiceMock.placeOrder).toHaveBeenCalled();
    expect(component.errorMessages.size).toBe(0);
    expect(component.problematicOrderIds.size).toBe(0);
  });

  it('should handle confirming order, with errors and modifications', () => {
    const updateOrdersSpy = spyOn(orderServiceMock, 'updateOrders');

    component.confirmOrder();
    fixture.detectChanges();

    expect(orderServiceMock.placeOrder).toHaveBeenCalled();
    expect(component.errorMessages.size).toBe(2);
    expect(component.problematicOrderIds.size).toBe(2);
    component.cartItems$.subscribe((items) => {
      expect(items.length).toBe(3);
    });
    expect(updateOrdersSpy).toHaveBeenCalled();
  });

  it('should get orders when updateOrders emits', () => {
    const getOrdersSpy = spyOn(component, 'getOrders');
    orderServiceMock.updateOrders({} as Order);
    expect(getOrdersSpy).toHaveBeenCalled();
  });
});
