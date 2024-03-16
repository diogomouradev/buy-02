import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';
import { ProductContainerComponent } from './product-container.component';
import { BehaviorSubject, of, Subject } from 'rxjs';
import { ProductService } from '../service/product.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { ProductCardComponent } from './product-card/product-card.component';
import { MatIconModule } from '@angular/material/icon';
import { ProductCardModalComponent } from './product-card-modal/product-card-modal.component';
import { MatTabsModule } from '@angular/material/tabs';
import { Product } from '../interfaces/product';
import {
  BrowserAnimationsModule,
  NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { CookieService } from 'ngx-cookie-service';
import { StateService } from '../service/state.service';
// import { DataService } from '../service/data.service';

describe('ProductContainerComponent', () => {
  let component: ProductContainerComponent;
  let fixture: ComponentFixture<ProductContainerComponent>;

  const mockProducts = [
    {
      id: '1',
      name: 'Product 1',
      price: 10.0,
      quantity: 5,
      description: 'This is product 1',
    },
    {
      id: '2',
      name: 'Product 2',
      price: 20.0,
      quantity: 3,
      description: 'This is product 2',
    },
  ];

  const mockUser = {
    name: 'test',
    email: 'test@test.com',
    id: 'testId',
    role: 'SELLER',
  };

  const productServiceMock = {
    getProducts: jasmine.createSpy('getProducts').and.returnValue(
      of(mockProducts),
    ),
    productAddedSource: new Subject<Product>(),
    productAdded$: of({} as Product),
    updateProductAdded: (value: Product) => {
      productServiceMock.productAddedSource.next(value);
    },
    getProductsById: jasmine.createSpy('getProductById').and.returnValue(of([
      {
        id: '2',
        name: 'Product 2',
        price: 20.0,
        quantity: 3,
        description: 'This is product 2',
      },
    ])),
  };

  const dataServiceMock = {
    dashboard$: of(false),
    dashboardSource: new BehaviorSubject<boolean>(false),
    updateDashboard: (value: boolean) => {
      dataServiceMock.dashboardSource.next(value);
    },
  };

  dataServiceMock.dashboard$ = dataServiceMock.dashboardSource.asObservable();

  productServiceMock.productAdded$ = productServiceMock.productAddedSource
    .asObservable();

  const cookieServiceMock = {
    get: jasmine.createSpy('get').and.returnValue('cookie'),
  };

  const mockStateService = jasmine.createSpyObj('StateService', [
    'getStateAsObservable',
  ]);
  mockStateService.getStateAsObservable.and.returnValue(of(mockUser));

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        ProductContainerComponent,
        ProductCardComponent,
        ProductCardModalComponent,
      ],
      imports: [
        HttpClientTestingModule,
        MatTabsModule,
        MatIconModule,
        NoopAnimationsModule,
        BrowserAnimationsModule,
      ],
      providers: [{
        provide: ProductService,
        useValue: productServiceMock,
      }, {
        provide: CookieService,
        useValue: cookieServiceMock,
        // }, {
        //   provide: DataService,
        //   useValue: dataServiceMock,
      }, {
        provide: StateService,
        useValue: mockStateService,
      }],
    }).compileComponents();
    fixture = TestBed.createComponent(ProductContainerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize user$ and currentUser', () => {
    const userSpy = spyOn(component.user$, 'next');
    component.ngOnInit();
    expect(userSpy).toHaveBeenCalledWith(mockUser);
    expect(component.currentUser).toEqual(mockUser);
  });

  it(
    'should display products when there are products',
    () => {
      const showProductsSpy = spyOn(component, 'showProducts');
      component.products$ = of(mockProducts);
      component.dashboard = false;
      productServiceMock.updateProductAdded({} as Product);

      const productElements = fixture.nativeElement.querySelectorAll(
        'app-product-card',
      );
      expect(showProductsSpy).toHaveBeenCalled();
      expect(productElements.length).toBe(2);
    },
  );

  it(
    'should call showProducts when productAdded$ emits and dashboard is false',
    () => {
      const showProductsSpy = spyOn(component, 'showProducts');
      component.dashboard = false;
      productServiceMock.updateProductAdded({} as Product);
      expect(showProductsSpy).toHaveBeenCalled();
    },
  );

  it(
    'should call getOwnerProduct when productAdded$ emits and dashboard is true',
    () => {
      const getOwnerProductsSpy = spyOn(component, 'getOwnerProducts');
      component.dashboard = true;
      productServiceMock.updateProductAdded({} as Product);
      expect(getOwnerProductsSpy).toHaveBeenCalled();
    },
  );

  it(
    'should handle the case when there are no products and user is at home',
    fakeAsync(() => {
      component.dashboard = false;
      productServiceMock.getProducts.and.returnValue(of([]));
      productServiceMock.updateProductAdded({} as Product);
      tick();
      const productElements = fixture.nativeElement.querySelectorAll(
        'app-product-card',
      );
      expect(productElements.length).toBe(0);
    }),
  );

  it(
    'should handle the case when there are no products and user is at dashboard',
    fakeAsync(() => {
      component.dashboard = true;
      productServiceMock.updateProductAdded({} as Product);
      tick();
      const productElements = fixture.nativeElement.querySelectorAll(
        'app-product-card',
      );
      expect(productElements.length).toBe(1);
    }),
  );

  it('should toggle dashboard when dashboard$ emits', () => {
    component.dashboard = false;
    component.toggleDashboard(true);
    expect(component.dashboard).toBe(true);
    expect(component.showAddButton).toBe(true);
  });
});
