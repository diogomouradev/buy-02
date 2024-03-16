import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OrderCardComponent } from './order-card.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { OrderService } from '../service/order.service';
import { UserService } from '../service/user.service';
import { of, Subject } from 'rxjs';
import { CartItem, Order } from '../interfaces/order';
import { AggregatedProduct, Product } from '../interfaces/product';
import { Router } from '@angular/router';

describe('OrderCardComponent', () => {
  let component: OrderCardComponent;
  let fixture: ComponentFixture<OrderCardComponent>;
  //eslint-disable-next-line
  let mockUserService: any;
  //eslint-disable-next-line
  let mockOrderService: any;
  //eslint-disable-next-line
  let mockSellerUser: any;
  //eslint-disable-next-line
  let mockBuyerUser: any;
  //eslint-disable-next-line
  let mockUser: any;
  let mockCartItems: CartItem[];
  let mockProducts: Product[];
  let mockAggregatedProduct: AggregatedProduct;
  //eslint-disable-next-line
  let mockRouter: any;

  beforeEach(() => {
    mockSellerUser = {
      name: 'seller',
      email: 'test@seller.com',
      id: 'sellerId',
      role: 'SELLER',
    };

    mockBuyerUser = {
      name: 'buyer',
      email: 'test@buyer.com',
      id: 'buyerId',
      role: 'USER',
    };

    mockUser = {
      name: 'buyer',
      email: 'test@buyer.com',
      id: 'buyerId',
      role: 'USER',
    };

    mockProducts = [
      {
        name: 'test123',
        quantity: 5,
        description: 'test desc',
        price: 10,
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

    mockAggregatedProduct = {
      product: mockProducts[0],
      totalPrice: 50.00,
      totalQuantity: 5,
    };

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

    mockUserService = {
      getOwnerInfo: jasmine.createSpy().and.returnValue(of(mockSellerUser)),
    };

    mockOrderService = {
      changeOrderStatus: jasmine.createSpy().and.returnValue(
        of(mockCartItems[0] as Order),
      ),
      reOrder: jasmine.createSpy(),
      removeItem: jasmine.createSpy(),
      orderUpdateSource: new Subject<Order>(),
      orderUpdates$: of({} as Order),
      updateOrders: (value: Order) => {
        mockOrderService.orderUpdateSource.next(value);
      },
    };

    mockOrderService.orderUpdates$ = mockOrderService.orderUpdateSource
      .asObservable();

    mockRouter = {
      url: '/shopcart',
    };

    TestBed.configureTestingModule({
      declarations: [OrderCardComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: UserService, useValue: mockUserService },
        { provide: OrderService, useValue: mockOrderService },
        { provide: Router, useValue: mockRouter },
      ],
    });
    fixture = TestBed.createComponent(OrderCardComponent);
    component = fixture.componentInstance;

    component.card = mockCartItems[0];
    component.filter = 'PENDING';
    component.user = mockUser;
    component.aggregatedProduct = mockAggregatedProduct;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with the given inputs', () => {
    expect(component.card).toEqual(mockCartItems[0]);
    expect(component.filter).toEqual('PENDING');
    expect(component.user).toEqual(mockUser);
    expect(component.isAggregatedProduct).toEqual(true);
  });

  it('should set the booleans based on route: /dashboard', () => {
    expect(component.isOnShopcart).toEqual(true);
    expect(component.isOnDashboard).toEqual(false);
  });

  it('should set the booleans based on route: /shopcart', () => {
    mockRouter.url = '/dashboard';
    component.ngOnInit();
    expect(component.isOnShopcart).toEqual(false);
    expect(component.isOnDashboard).toEqual(true);
  });

  it('should call changeOrderStatus and update order', () => {
    const newStatus = 'CONFIRMED';
    component.changeOrderStatus(newStatus);

    expect(mockOrderService.changeOrderStatus).toHaveBeenCalledWith(
      component.card.id,
      newStatus,
    );
    mockOrderService.orderUpdates$.subscribe((order: Order) => {
      expect(order).toEqual(mockCartItems[0] as Order);
    });
  });

  it('should handle different user roles', () => {
    component.user = mockSellerUser;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.isSeller).toEqual(true);

    component.user = mockBuyerUser;
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.isSeller).toEqual(false);
  });

  it('should call reOrder', () => {
    component.reOrder(component.card.id!);
    expect(mockOrderService.reOrder).toHaveBeenCalledWith(component.card.id);
  });

  it('should call removeItem', () => {
    component.removeItem();
    expect(mockOrderService.removeItem).toHaveBeenCalledWith(component.card.id);
  });
});
