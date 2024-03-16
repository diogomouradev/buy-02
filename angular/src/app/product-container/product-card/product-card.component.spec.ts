import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductCardComponent } from './product-card.component';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MediaService } from 'src/app/service/media.service';
import { ProductCardModalComponent } from '../product-card-modal/product-card-modal.component';
import { MatTabsModule } from '@angular/material/tabs';
import { MatIconModule } from '@angular/material/icon';
import {
  BrowserAnimationsModule,
  NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { of, Subject } from 'rxjs';
import { FormStateService } from 'src/app/service/form-state.service';
import { DestroyRef, ElementRef } from '@angular/core';
import { DataService } from 'src/app/service/data.service';
import { AuthService } from 'src/app/service/auth.service';
import { Media } from 'src/app/interfaces/media';

describe('ProductCardComponent', () => {
  let component: ProductCardComponent;
  let fixture: ComponentFixture<ProductCardComponent>;

  const mockProduct = {
    name: 'test product',
    quantity: 10,
    description: 'test description',
    price: 123,
    userId: '123123123',
    id: '908908908',
    thumbnail: 'image.jpg',
    image: 'image.jpg',
  };

  const mockUser = {
    name: 'ali',
    email: 'ali@gmail.com',
    jwtToken: '123123123123123',
    id: 'temp123123123',
    role: 'SELLER',
    avatar: 'avatar.jpg',
  };

  const formStateServiceMock = {
    setFormOpen: jasmine.createSpy('setFormOpen'),
    formOpen$: of(false),
  };

  const imageAddedSubject = new Subject<Media>();

  const mediaServiceMock = {
    getProductThumbnail: jasmine.createSpy('getProductThumbnail').and
      .returnValue(of({
        id: '123123123',
        image: '987987987987',
        productId: '123123123',
        userId: '123123123',
        mimeType: 'image/jpg',
      })),
    getProductMedia: jasmine.createSpy('getProductMedia').and.returnValue(
      of([]),
    ),
    formatMedia: jasmine.createSpy('formatMedia').and.returnValue(
      'data:' + 'image/jpg' + ';base64,' + '987987987987',
    ),
    imageAdded$: imageAddedSubject.asObservable(),
  };

  const authServiceMock = {
    getAuth: jasmine.createSpy('getAuth').and.returnValue(of({
      name: 'ali',
      email: 'ali@gmail.com',
      jwtToken: '123123123123123',
      id: 'temp123123123',
      role: 'SELLER',
      avatar: 'avatar.jpg',
    })),
  };

  const dataServiceMock = {
    ids$: of(['123123123', '123123123']),
    sendProductId: jasmine.createSpy('sendProductId'),
    deleteImage$: of('123123123'),
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [
        ProductCardComponent,
        ProductCardModalComponent,
      ],
      imports: [
        HttpClientTestingModule,
        MatTabsModule,
        MatIconModule,
        BrowserAnimationsModule,
        NoopAnimationsModule,
      ],
      providers: [
        {
          provide: FormStateService,
          useValue: formStateServiceMock,
        },
        {
          provide: MediaService,
          useValue: mediaServiceMock,
        },
        {
          provide: DataService,
          useValue: dataServiceMock,
        },
        {
          provide: AuthService,
          useValue: authServiceMock,
        },
        {
          provide: 'product',
          useValue: mockProduct,
        },
        DestroyRef,
      ],
    });
    fixture = TestBed.createComponent(ProductCardComponent);
    component = fixture.componentInstance;
    component.productModal = new ElementRef(document.createElement('dialog'));
    component.container = new ElementRef(document.createElement('div'));
    component.currentUser = mockUser;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with modalVisible as false', () => {
    expect(component.modalVisible).toBeFalse();
  });

  it(
    'showModal should set modalVisible to true and open the modal',
    () => {
      component.productModal = {
        nativeElement: { show: jasmine.createSpy() },
      } as ElementRef;
      expect(component.modalVisible).toBeFalse();

      component.showModal();

      expect(component.modalVisible).toBeTrue();
      expect(formStateServiceMock.setFormOpen).toHaveBeenCalledWith(true);
      expect(component.productModal?.nativeElement).toBeDefined();
    },
  );

  it('hideModal should set modalVisible to false and close the modal', () => {
    component.hideModal();
    expect(formStateServiceMock.setFormOpen).toHaveBeenCalledWith(false);
    expect(component.modalVisible).toBeFalse();
  });

  it('should get the current user if user is logged in', () => {
    expect(component.currentUser).toEqual({
      name: 'ali',
      email: 'ali@gmail.com',
      jwtToken: '123123123123123',
      id: 'temp123123123',
      role: 'SELLER',
      avatar: 'avatar.jpg',
    });
  });

  it(
    'should add blur-filter calss to container when form is open and remove it when closed',
    () => {
      component.container = new ElementRef(document.createElement('div'));
      formStateServiceMock.formOpen$ = of(true);
      component.ngAfterViewInit();
      expect(
        component.container?.nativeElement.classList.contains('blur-filter'),
      )
        .toBeTrue();

      formStateServiceMock.formOpen$ = of(false);
      component.ngAfterViewInit();
      expect(
        component.container?.nativeElement.classList.contains('blur-filter'),
      )
        .toBeFalse();
    },
  );

  it('should render product name, price and quantity', () => {
    component.product = {
      name: 'Test product',
      price: 123.45,
      quantity: 10,
      description: 'Test description',
    };
    fixture.detectChanges();
    const title = fixture.nativeElement.querySelector('.product-title');
    const price = fixture.nativeElement.querySelector('.product-price');
    const quantity = fixture.nativeElement.querySelector('.product-quantity');
    expect(title.textContent).toBe(`${component.product.name}`);
    expect(price.textContent).toBe(`$${component.product.price}`);
    expect(quantity.textContent).toBe(
      `items left: ${component.product.quantity}`,
    );
  });

  it('should call showModal() when the card is clicked', () => {
    spyOn(component, 'showModal');

    const card = fixture.nativeElement.querySelector('.card-container');
    card.click();
    expect(component.showModal).toHaveBeenCalled();
  });
});
