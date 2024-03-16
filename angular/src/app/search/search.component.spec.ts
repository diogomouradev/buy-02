import {
  ComponentFixture,
  fakeAsync,
  TestBed,
  tick,
} from '@angular/core/testing';

import { SearchComponent } from './search.component';
import { environment } from 'src/environments/environment';
import { Product } from '../interfaces/product';
import { of } from 'rxjs';
import { ProductService } from '../service/product.service';
import { StateService } from '../service/state.service';
import { MatIconModule } from '@angular/material/icon';
import { FormsModule } from '@angular/forms';
import { ProductCardComponent } from '../product-container/product-card/product-card.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';

describe('SearchComponent', () => {
  let component: SearchComponent;
  let fixture: ComponentFixture<SearchComponent>;
  //eslint-disable-next-line
  let mockUser: any;
  let mockProducts: Product[];

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
        userId: '1',
        id: '123',
        thumbnail: environment.placeholder,
      },
      {
        name: 'test',
        quantity: 6,
        description: 'test desc2',
        price: 99.99,
        userId: '1',
        id: '1234',
        thumbnail: environment.placeholder,
      },
    ];

    const productServiceMock = {
      getProducts: jasmine.createSpy().and.returnValue(of(mockProducts)),
    };

    const stateServiceMock = {
      getStateAsObservable: jasmine.createSpy().and.returnValue(of(mockUser)),
    };
    TestBed.configureTestingModule({
      declarations: [SearchComponent, ProductCardComponent],
      imports: [MatIconModule, FormsModule, HttpClientTestingModule],
      providers: [
        { provide: ProductService, useValue: productServiceMock },
        { provide: StateService, useValue: stateServiceMock },
      ],
    });
    fixture = TestBed.createComponent(SearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch the products on init', () => {
    expect(component.products).not.toBeNull();
  });

  it('should subscribe to user state and assign currentUser', () => {
    expect(component.currentUser).toEqual(mockUser);
  });

  it('should reset search criteria and products', () => {
    component.searchText = 'test';
    component.minPrice = '10';
    component.maxPrice = '20';

    component.reset();

    expect(component.searchText).toBe('');
    expect(component.minPrice).toBe('');
    expect(component.maxPrice).toBe('');
    component.products$!.subscribe((products) => {
      expect(products).toEqual(mockProducts);
    });
  });

  it(
    'should filter products on search',
    fakeAsync(() => {
      component.searchText = 'test';
      component.minPrice = '20';
      component.maxPrice = '100';
      component.products = mockProducts;

      component.search();
      tick();

      component.products$!.subscribe((filteredProducts) => {
        expect(filteredProducts).toEqual([mockProducts[0]]);
      });
    }),
  );
});
