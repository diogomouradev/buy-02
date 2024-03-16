import { TestBed } from '@angular/core/testing';
import { ProductService } from './product.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { Product } from '../interfaces/product';
import { User } from '../interfaces/user';
import { of } from 'rxjs';
import { AuthService } from './auth.service';

describe('ProductService', () => {
  let service: ProductService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    const authServiceMock = {
      getAuth: () => {
        return of({
          name: 'test',
          email: 'test@test.com',
          id: '123',
          role: 'SELLER',
        } as User);
      },
    };
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [
        ProductService,
        { provide: AuthService, useValue: authServiceMock },
      ],
    });
    service = TestBed.inject(ProductService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should get products', () => {
    const mockProducts: Product[] = [
      {
        name: 'test',
        quantity: 5,
        description: 'test desc',
        price: 12.99,
        userId: '1',
        id: '123',
        thumbnail: environment.placeholder,
      },
      {
        name: 'test2',
        quantity: 6,
        description: 'test desc2',
        price: 12.99,
        userId: '1',
        id: '1234',
        thumbnail: environment.placeholder,
      },
    ];

    service.getProducts().subscribe((products) => {
      expect(products).toEqual(mockProducts);
    });

    const req = httpTestingController.expectOne(`${environment.productsURL}`);
    expect(req.request.method).toEqual('GET');
    req.flush(mockProducts);
  });

  it('should get owner', () => {
    const userId = '123';
    const mockUser: User = {
      name: 'test',
      email: 'test@test.com',
      id: '123',
      role: 'SELLER',
    };

    service.getOwner(userId).subscribe((user) => {
      expect(user).toEqual(mockUser);
    });

    const req = httpTestingController.expectOne(
      `${environment.productsURL}?userId=${userId}`,
    );
    expect(req.request.method).toEqual('GET');
    req.flush(mockUser);
  });

  it('should get products by user ID', () => {
    const userId = '123';
    const mockProducts: Product[] = [
      {
        name: 'test',
        quantity: 5,
        description: 'test desc',
        price: 12.99,
        userId: '123',
        id: '123',
        thumbnail: environment.placeholder,
      },
      {
        name: 'test2',
        quantity: 6,
        description: 'test desc2',
        price: 12.99,
        userId: '123',
        id: '1234',
        thumbnail: environment.placeholder,
      },
    ];

    service.getProductsById(userId).subscribe((products) => {
      expect(products).toEqual(mockProducts);
    });

    const req = httpTestingController.expectOne(
      `${environment.userProductsURL}` + userId,
    );
    expect(req.request.method).toEqual('GET');
    req.flush(mockProducts);
  });

  it('should get owner products', () => {
    const userId = '123';
    const mockProducts: Product[] = [
      {
        name: 'test',
        quantity: 5,
        description: 'test desc',
        price: 12.99,
        userId: '123',
        id: '123',
        thumbnail: environment.placeholder,
      },
      {
        name: 'test2',
        quantity: 6,
        description: 'test desc2',
        price: 12.99,
        userId: '123',
        id: '1234',
        thumbnail: environment.placeholder,
      },
    ];

    service.getProductsById(userId).subscribe((products) => {
      expect(products).toEqual(mockProducts);
      service.updateUserProducts(products);
    });

    const req = httpTestingController.expectOne(
      `${environment.userProductsURL}` + userId,
    );

    expect(req.request.method).toEqual('GET');
    req.flush(mockProducts);

    service.userProducts$.subscribe((products) => {
      expect(products).toEqual(mockProducts);
    });
  });

  it('should delete a product', () => {
    const productId = '123';

    service.deleteProduct(productId);

    const req = httpTestingController.expectOne(
      `${environment.productsURL}/${productId}`,
    );
    expect(req.request.method).toEqual('DELETE');
    req.flush({});
  });

  it('should add a product with media', () => {
    const mockForm = {
      name: 'test',
      description: 'test desc',
      price: 12.99,
      quantity: 50,
    };
    const mockMediaForm = new FormData();

    const mockProduct: Product = {
      name: 'test',
      quantity: 5,
      description: 'test desc',
      price: 12.99,
      userId: '123',
      id: '123',
      thumbnail: environment.placeholder,
    };

    service.addProduct(mockForm, mockMediaForm).subscribe((product) => {
      expect(product).toEqual(mockProduct);
    });

    const productCreationResponse = {
      product: mockProduct,
    };

    const req = httpTestingController.expectOne(`${environment.productsURL}`);
    expect(req.request.method).toEqual('POST');
    req.flush(productCreationResponse);
  });

  it('should add a product without media', () => {
    const mockForm = {
      name: 'test',
      description: 'test desc',
      price: 12.99,
      quantity: 50,
    };

    const mockProduct: Product = {
      name: 'test',
      quantity: 50,
      description: 'test desc',
      price: 12.99,
      userId: '123',
      id: '123',
      thumbnail: environment.placeholder,
    };

    service.addProduct(mockForm, null).subscribe((product) => {
      expect(product).toEqual(mockProduct);
    });

    const productCreationResponse = {
      product: mockProduct,
    };

    const req = httpTestingController.expectOne(`${environment.productsURL}`);
    expect(req.request.method).toEqual('POST');
    req.flush(productCreationResponse);
  });

  it('should handle error when adding a product', () => {
    const mockForm = {
      name: 'test',
      description: 'test desc',
      price: 12.99,
      quantity: 50,
    };
    const mockMediaForm = new FormData();

    service.addProduct(mockForm, mockMediaForm).subscribe((product) => {
      expect(product).toBeNull();
    });

    const errorResponse = {
      message: 'bad request',
      status: 400,
    };

    const req = httpTestingController.expectOne(`${environment.productsURL}`);
    expect(req.request.method).toEqual('POST');
    req.flush(errorResponse, {
      status: 500,
      statusText: 'Internal Server Error',
    });
  });

  it('should update productAdded$', () => {
    const mockProduct = {
      name: 'test',
      quantity: 5,
      description: 'test desc',
      price: 12.99,
      userId: '123',
      id: '123',
      thumbnail: environment.placeholder,
    };
    const spy = spyOn(service, 'updateProductAdded').and.callThrough();
    service.updateProductAdded(mockProduct);
    service.productAdded$.subscribe((product) => {
      expect(product).toEqual(mockProduct);
    });
    expect(spy).toHaveBeenCalled();
  });

  it('should update userProducts$', () => {
    const mockProducts = [
      {
        name: 'test',
        quantity: 5,
        description: 'test desc',
        price: 12.99,
        userId: '123',
        id: '123',
        thumbnail: environment.placeholder,
      },
      {
        name: 'test2',
        quantity: 5,
        description: 'test desc2',
        price: 12.99,
        userId: '123',
        id: '1234',
        thumbnail: environment.placeholder,
      },
    ] as Product[];
    const spy = spyOn(service, 'updateUserProducts').and.callThrough();
    service.updateUserProducts(mockProducts);
    service.userProducts$.subscribe((products) => {
      expect(products).toEqual(mockProducts);
    });
    expect(spy).toHaveBeenCalled();
  });
});
