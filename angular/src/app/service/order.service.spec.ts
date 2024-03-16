import { fakeAsync, TestBed, tick } from '@angular/core/testing';

import { OrderService } from './order.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import {
  Cart,
  CartItem,
  CartResponse,
  Order,
  OrderModifications,
  PersonalOrder,
} from '../interfaces/order';
import { AggregatedProduct, Product } from '../interfaces/product';
import { of } from 'rxjs';

describe('OrderService', () => {
  let service: OrderService;
  let mockPersonalOrder: PersonalOrder;
  let mockOrders: Order[];
  let mockProducts: Product[];
  let mockCartResponse: CartResponse;
  let mockCart: Cart;
  let mockCartItems: CartItem[];
  let mockOrderModifications: OrderModifications;
  let mockFailNotes: Set<string>;
  let mockAggregatedProducts: AggregatedProduct[];

  beforeEach(() => {
    mockProducts = [
      {
        name: 'product1',
        quantity: 1,
        description: 'product1 description',
        price: 20,
        userId: 'seller1',
        id: 'product1',
      },
      {
        name: 'product2',
        quantity: 2,
        description: 'product2 description',
        price: 20,
        userId: 'seller2',
        id: 'product2',
      },
      {
        name: 'product3',
        quantity: 3,
        description: 'product3 description',
        price: 30,
        userId: 'seller3',
        id: 'product3',
      },
    ];

    mockAggregatedProducts = [
      {
        product: mockProducts[1],
        totalPrice: 40,
        totalQuantity: 2,
      },
      {
        product: mockProducts[2],
        totalPrice: 90,
        totalQuantity: 3,
      },
    ];

    mockOrders = [
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
        status: 'CONFIRMED',
        buyerId: 'buyer2',
        sellerId: 'seller2',
        product: mockProducts[1],
        quantity: 2,
      },
      {
        id: '3',
        status: 'CANCELLED',
        buyerId: 'buyer3',
        sellerId: 'seller3',
        product: mockProducts[2],
        quantity: 3,
      },
      {
        id: '4',
        status: 'CONFIRMED',
        buyerId: 'buyer4',
        sellerId: 'seller4',
        product: mockProducts[2],
        quantity: 3,
      },
    ];

    mockPersonalOrder = {
      confirmed: [mockOrders[1], mockOrders[3]],
      pending: [mockOrders[0]],
      cancelled: [mockOrders[2]],
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

    mockCart = {
      orders: mockCartItems,
    };

    mockCartResponse = {
      cart: mockCart,
      processed: false,
      orderModifications: mockOrderModifications,
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

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [OrderService],
    });
    service = TestBed.inject(OrderService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should retrieve all orders', () => {
    const mockOrders: PersonalOrder = mockPersonalOrder;
    const httpTestingController = TestBed.inject(HttpTestingController);

    service.getAllOrders().subscribe((orders) => {
      expect(orders).toEqual(mockOrders);
    });

    const req = httpTestingController.expectOne(environment.ordersURL);
    expect(req.request.method).toEqual('GET');
    req.flush(mockOrders);
  });

  it('should place an order', () => {
    const mockResponse: CartResponse = mockCartResponse;
    const httpTestingController = TestBed.inject(HttpTestingController);

    service.placeOrder().subscribe((response) => {
      expect(response).toEqual(mockResponse);
    });

    const req = httpTestingController.expectOne(environment.ordersURL);
    expect(req.request.method).toEqual('POST');
    req.flush(mockResponse);
  });

  it('should change the order status', () => {
    const orderId = '1';
    const status = 'PENDING';
    const mockOrder: Order = mockOrders[0];
    const httpTestingController = TestBed.inject(HttpTestingController);

    service.changeOrderStatus(orderId, status).subscribe((order) => {
      expect(order).toEqual(mockOrder);
    });

    const req = httpTestingController.expectOne(`${environment.ordersURL}`);
    expect(req.request.method).toEqual('PUT');
    expect(req.request.body).toEqual({ id: orderId, status });
    req.flush(mockOrder);
  });

  it('should remove item from shopping cart', () => {
    const orderId = '1';

    service.removeItem(orderId);

    const req = TestBed.inject(HttpTestingController).expectOne(
      `${environment.cartURL}/${orderId}`,
    );
    expect(req.request.method).toEqual('DELETE');
    req.flush(null);
  });

  it('should update filter type', () => {
    const newFilterType = 'CONFIRMED';
    service.setFilterType(newFilterType);

    service.filterType$.subscribe((filterType) => {
      expect(filterType).toBe(newFilterType);
    });
  });

  it(
    'should reset filter type after a delay',
    fakeAsync(() => {
      service.resetFilterType();
      tick(16);
      service.filterType$.subscribe((filterType) => {
        expect(filterType).toBe('PENDING');
      });
    }),
  );

  it(
    'should emit a new order update',
    () => {
      const updateSpy = spyOn(service, 'updateOrders');
      const newOrder: Order = mockOrders[1];
      service.updateOrders(newOrder);
      service.orderUpdates$.subscribe((order) => {
        expect(order).toEqual(newOrder);
      });
      expect(updateSpy).toHaveBeenCalled();
    },
  );

  it(
    'should get aggregated products',
    () => {
      const mockOrders: PersonalOrder = mockPersonalOrder;
      const expectedAggregatedProducts: AggregatedProduct[] =
        mockAggregatedProducts;
      spyOn(service, 'getAllOrders').and.returnValue(of(mockOrders));

      service.getAggregatedProducts().subscribe((aggregatedProducts) => {
        expect(aggregatedProducts).toEqual(expectedAggregatedProducts);
      });
    },
  );

  it('should get the shopping cart with no filtering', () => {
    const getCartSpy = spyOn(service, 'getCartFromDB').and.returnValue(
      of(mockCart),
    );
    service.getShoppingCart().subscribe((cart) => {
      expect(cart).toEqual(mockCart.orders);
    });
    expect(getCartSpy).toHaveBeenCalled();
  });

  it('should add a product to the cart', () => {
    const mockProduct: Product = mockProducts[0];
    const quantity = 1;
    const httpTestingController = TestBed.inject(HttpTestingController);
    const mockOrderRequest = {
      status: 'PENDING',
      buyerId: undefined,
      sellerId: mockProducts[0].userId,
      product: mockProducts[0],
      quantity: 1,
    };

    service.addToCart(mockProduct, quantity);

    const req = httpTestingController.expectOne(environment.addToCartURL);
    expect(req.request.method).toEqual('POST');
    expect(req.request.body).toEqual(mockOrderRequest);
    req.flush(mockProducts[0]);
  });

  it('should create a new order', () => {
    const mockProduct: Product = mockProducts[1];
    const quantity = 1;
    const order = service.createOrder(mockProduct, quantity);

    expect(order.status).toBe('PENDING');
    expect(order.product).toEqual(mockProduct);
    expect(order.quantity).toBe(quantity);
  });

  it('should reorder a product', () => {
    const orderId = '123';
    const httpTestingController = TestBed.inject(HttpTestingController);

    service.reOrder(orderId);

    const req = httpTestingController.expectOne(
      `${environment.ordersURL}?reorder=${orderId}`,
    );
    expect(req.request.method).toEqual('POST');
    expect(req.request.params.get('reorder')).toBe(orderId);
    req.flush(null);
  });

  it('should filter orders based on given criteria: "CONFIRMED"', () => {
    const localCartItems: CartItem[] = mockCartItems;
    const filteredItems = service.filterOrders('CONFIRMED', localCartItems);

    filteredItems.subscribe((items) => {
      expect(items).toEqual([localCartItems[2]]);
    });
  });

  it('should filter orders based on given criteria: "CANCELLED"', () => {
    const localCartItems: CartItem[] = mockCartItems;
    const filteredItems = service.filterOrders('CANCELLED', localCartItems);

    filteredItems.subscribe((items) => {
      expect(items).toEqual([localCartItems[1]]);
    });
  });

  it('should filter orders based on given criteria: "PENDING"', () => {
    const localCartItems: CartItem[] = mockCartItems;
    const filteredItems = service.filterOrders('PENDING', localCartItems);

    filteredItems.subscribe((items) => {
      expect(items).toEqual([localCartItems[0]]);
    });
  });
});
