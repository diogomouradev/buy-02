import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { DashboardComponent } from './dashboard.component';
import { OrderService } from '../service/order.service';
import { StateService } from '../service/state.service';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTooltipModule } from '@angular/material/tooltip';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { CartItem, Order, PersonalOrder } from '../interfaces/order';
import { AggregatedProduct } from '../interfaces/product';
import { FormsModule } from '@angular/forms';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { OrderCardComponent } from '../order-card/order-card.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('DashboardComponent', () => {
  let component: DashboardComponent;
  let fixture: ComponentFixture<DashboardComponent>;
  let mockOrderService: jasmine.SpyObj<OrderService>;
  let mockStateService: jasmine.SpyObj<StateService>;
  let filterTypeSubject: BehaviorSubject<string>;
  let orderUpdatesSubject: Subject<Order>;
  let cartItemsSubject: BehaviorSubject<CartItem[]>;

  const mockProduct1 = {
    name: 'product1',
    quantity: 1,
    description: 'description1',
    price: 100,
    userId: 'userId1',
    id: '1',
  };

  const mockProduct2 = {
    name: 'product2',
    quantity: 2,
    description: 'description2',
    price: 200,
    userId: 'userId2',
    id: '2',
  };

  const mockProduct3 = {
    name: 'product3',
    quantity: 3,
    description: 'description3',
    price: 300,
    userId: 'userId3',
    id: '3',
  };

  const mockPendingOrder = {
    id: '1',
    status: 'PENDING',
    buyerId: 'userId1',
    sellerId: 'userId2',
    product: mockProduct1,
    quantity: 1,
  };

  const mockConfirmedOrder = {
    id: '2',
    status: 'CONFIRMED',
    buyerId: 'userId1',
    sellerId: 'userId2',
    product: mockProduct2,
    quantity: 2,
  };

  const mockCancelledOrder = {
    id: '3',
    status: 'CANCELLED',
    buyerId: 'userId1',
    sellerId: 'userId2',
    product: mockProduct3,
    quantity: 3,
  };

  const mockAggregatedProducts = [{
    product: mockProduct1,
    totalPrice: 100,
    totalQuantity: 1,
  }, {
    product: mockProduct2,
    totalPrice: 400,
    totalQuantity: 2,
  }] as AggregatedProduct[];

  const mockUser = {
    name: 'test',
    email: 'test@test.com',
    id: 'testId',
    role: 'SELLER',
  };

  beforeEach(async () => {
    filterTypeSubject = new BehaviorSubject<string>('PENDING');
    orderUpdatesSubject = new Subject<Order>();
    cartItemsSubject = new BehaviorSubject<CartItem[]>([]);

    mockOrderService = jasmine.createSpyObj('OrderService', [
      'getAllOrders',
      'getAggregatedProducts',
      'setFilterType',
      'resetFilterType',
    ]);
    mockStateService = jasmine.createSpyObj('StateService', [
      'getStateAsObservable',
    ]);

    mockOrderService.getAllOrders.and.returnValue(of({
      pending: [mockPendingOrder],
      confirmed: [mockConfirmedOrder],
      cancelled: [mockCancelledOrder],
    } as PersonalOrder));

    mockOrderService.getAggregatedProducts.and.returnValue(
      of(mockAggregatedProducts),
    );

    mockStateService.getStateAsObservable.and.returnValue(of(mockUser));

    mockOrderService.filterType$ = filterTypeSubject.asObservable();
    mockOrderService.orderUpdates$ = orderUpdatesSubject.asObservable();
    mockOrderService.cartItems$ = cartItemsSubject.asObservable();

    await TestBed.configureTestingModule({
      declarations: [DashboardComponent, OrderCardComponent],
      providers: [
        { provide: OrderService, useValue: mockOrderService },
        { provide: StateService, useValue: mockStateService },
      ],
      imports: [
        HttpClientTestingModule,
        MatIconModule,
        MatTabsModule,
        MatTooltipModule,
        FormsModule,
        BrowserAnimationsModule,
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DashboardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it(
    'should create',
    () => {
      expect(component).toBeTruthy();
    },
  );

  it(
    'should initialize with default values and call services',
    () => {
      expect(component.empty).toBeTrue();
      expect(mockStateService.getStateAsObservable).toHaveBeenCalled();
      expect(mockOrderService.getAllOrders).toHaveBeenCalled();
    },
  );

  it('should subscribe to user$ and set currentUser and isSeller on initialization', () => {
    mockStateService.getStateAsObservable.and.returnValue(of(mockUser));
    fixture.detectChanges();

    component.user$?.subscribe((user) => {
      expect(user).toEqual(mockUser);
      expect(component.currentUser).toEqual(mockUser);
      expect(component.isSeller).toBeTrue();
    });
  });

  it('should react to filterType$ changes from OrderService', () => {
    const newFilterType = 'CONFIRMED';
    filterTypeSubject.next(newFilterType);
    fixture.detectChanges();

    mockOrderService.filterType$.subscribe((filterType) => {
      expect(filterType).toBe(newFilterType);
      expect(component.filterType).toBe(newFilterType);
    });
  });

  describe('Total Amount Based on Filter', () => {
    beforeEach(() => {
      component.getAllOrders();
    });

    it('should update totalAmount correctly for PENDING orders', () => {
      filterTypeSubject.next('PENDING');
      fixture.detectChanges();
      expect(component.pendingTotalPrice).toBe('100.00');
    });

    it('should update totalAmount correctly for CONFIRMED orders', () => {
      filterTypeSubject.next('CONFIRMED');
      fixture.detectChanges();
      expect(component.confirmedTotalPrice).toBe('400.00');
    });

    it('should update totalAmount correctly for CANCELLED orders', () => {
      filterTypeSubject.next('CANCELLED');
      fixture.detectChanges();
      expect(component.cancelledTotalPrice).toBe('900.00');
    });
  });

  describe('Search Bar Filtering', () => {
    beforeEach(() => {
      component.getAllOrders();
      fixture.detectChanges();
    });

    it('should filter pending orders based on the search term', () => {
      component.searchTerm = 'product1';
      component.filterOrders(component.searchTerm);
      fixture.detectChanges();

      component.pendingOrders$.subscribe((filteredOrders) => {
        expect(filteredOrders.length).toBe(1);
        expect(filteredOrders[0].product.name).toContain('product1');
      });
    });

    it('should filter cancelled orders based on the search term', () => {
      component.searchTerm = 'product3';
      component.filterOrders(component.searchTerm);
      fixture.detectChanges();

      component.cancelledOrders$.subscribe((filteredOrders) => {
        expect(filteredOrders.length).toBe(1);
        expect(filteredOrders[0].product.name).toContain('product3');
      });
    });

    it('should filter confirmed orders based on the search term', () => {
      component.searchTerm = 'product2';
      component.filterOrders(component.searchTerm);
      fixture.detectChanges();

      component.confirmedOrders$.subscribe((filteredOrders) => {
        expect(filteredOrders.length).toBe(1);
        expect(filteredOrders[0].product.name).toContain('product2');
      });
    });
  });

  describe('changeFilter Method', () => {
    it('should set correct totalAmount and call setFilterType with PENDING', () => {
      component.changeFilter('PENDING');
      expect(component.totalAmount).toBe(component.pendingTotalPrice);
      expect(mockOrderService.setFilterType).toHaveBeenCalledWith('PENDING');
    });

    it('should set correct totalAmount and call setFilterType with CONFIRMED', () => {
      component.changeFilter('CONFIRMED');
      expect(component.totalAmount).toBe(component.confirmedTotalPrice);
      expect(mockOrderService.setFilterType).toHaveBeenCalledWith('CONFIRMED');
    });

    it('should set correct totalAmount and call setFilterType with CANCELLED', () => {
      component.changeFilter('CANCELLED');
      expect(component.totalAmount).toBe(component.cancelledTotalPrice);
      expect(mockOrderService.setFilterType).toHaveBeenCalledWith('CANCELLED');
    });

    it('should set correct totalAmount and call setFilterType with BEST PRODUCTS', () => {
      component.changeFilter('BEST PRODUCTS');
      expect(component.totalAmount).toBe(component.confirmedTotalPrice);
      expect(mockOrderService.setFilterType).toHaveBeenCalledWith(
        'BEST PRODUCTS',
      );
    });
  });

  describe('Aggregated Products Functionality', () => {
    it(
      'should display aggregated products correctly when filter type is BEST PRODUCTS',
      fakeAsync(() => {
        filterTypeSubject.next('BEST PRODUCTS');
        fixture.detectChanges();
        tick(500);

        component.aggregatedProducts$.subscribe((aggregatedProducts) => {
          expect(aggregatedProducts.length).toBe(2);
          expect(aggregatedProducts).toEqual(mockAggregatedProducts);
          expect(aggregatedProducts[0].totalPrice).toBe(400);
          expect(aggregatedProducts[1].totalPrice).toBe(100);
        });
      }),
    );
  });

  describe('Filtering Aggregated Products', () => {
    beforeEach(() => {
      filterTypeSubject.next('BEST PRODUCTS');
      fixture.detectChanges();
    });

    it('should filter aggregated products based on the search term: "product1"', () => {
      component.searchTerm = 'product1';
      component.filterOrders(component.searchTerm);
      fixture.detectChanges();

      component.aggregatedProducts$.subscribe((filteredAggregatedProducts) => {
        expect(filteredAggregatedProducts.length).toBe(1);
        expect(filteredAggregatedProducts[0].product.name).toContain(
          'product1',
        );
      });
    });

    it('should filter aggregated products based on the search term: "product2"', () => {
      component.searchTerm = 'product2';
      component.filterOrders(component.searchTerm);
      fixture.detectChanges();

      component.aggregatedProducts$.subscribe((filteredAggregatedProducts) => {
        expect(filteredAggregatedProducts.length).toBe(1);
        expect(filteredAggregatedProducts[0].product.name).toContain(
          'product2',
        );
      });
    });
  });

  describe('NgOnDestroy and Reset functionalities', () => {
    it('reset should clear the search term and call filterOrders', () => {
      const filterSpy = spyOn(component, 'filterOrders');
      component.searchTerm = 'test';
      component.reset();
      expect(component.searchTerm).toBe('');
      expect(filterSpy).toHaveBeenCalledWith('');
    });

    it('ngOnDestroy should call resetFilterType on OrderService', () => {
      component.ngOnDestroy();
      expect(mockOrderService.resetFilterType).toHaveBeenCalled();
    });
  });
});
