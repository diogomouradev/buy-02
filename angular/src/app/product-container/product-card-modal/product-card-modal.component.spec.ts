import { ComponentFixture, TestBed } from '@angular/core/testing';
import { ProductCardModalComponent } from './product-card-modal.component';
import { MediaService } from 'src/app/service/media.service';
import { HttpClientTestingModule } from '@angular/common/http/testing';
import { MatTabsModule } from '@angular/material/tabs';
import {
  BrowserAnimationsModule,
  NoopAnimationsModule,
} from '@angular/platform-browser/animations';
import { AuthService } from 'src/app/service/auth.service';
import { environment } from 'src/environments/environment';
import { of } from 'rxjs';
import { DataService } from 'src/app/service/data.service';
import { UserService } from 'src/app/service/user.service';
import { MatIconModule } from '@angular/material/icon';
import { FormControl, FormGroup, FormsModule } from '@angular/forms';
import { FormStateService } from 'src/app/service/form-state.service';
import { FileSelectEvent } from 'primeng/fileupload';
import { ProductService } from 'src/app/service/product.service';
import { ProductRequest } from 'src/app/interfaces/product-request';

describe('ProductCardModalComponent', () => {
  let component: ProductCardModalComponent;
  let fixture: ComponentFixture<ProductCardModalComponent>;
  //eslint-disable-next-line
  let authService: AuthService;
  //eslint-disable-next-line
  let mediaService: MediaService;
  //eslint-disable-next-line
  let dataService: DataService;
  //eslint-disable-next-line
  let userService: UserService;
  let productService: ProductService;

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
      quantity: 6,
      description: 'test desc2',
      price: 12.99,
      userId: '123',
      id: '1234',
      thumbnail: environment.placeholder,
    },
  ];

  const mockMediaResponse = {
    productId: '123',
    media: [
      {
        id: '2',
        image: 'testimage',
        productId: '123',
        userId: '',
        mimeType: 'image/png',
      },
      {
        id: '5',
        image: 'testimage',
        productId: '123',
        userId: '',
        mimeType: 'image/png',
      },
    ],
  };

  const mockUser = {
    name: 'test',
    email: 'test@test.com',
    id: '123',
    role: 'SELLER',
    avatar: 'testimage',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [ProductCardModalComponent],
      imports: [
        HttpClientTestingModule,
        MatTabsModule,
        NoopAnimationsModule,
        BrowserAnimationsModule,
        MatIconModule,
        FormsModule,
      ],
      providers: [
        MediaService,
        AuthService,
        DataService,
        UserService,
        FormStateService,
        ProductService,
      ],
    });
    fixture = TestBed.createComponent(ProductCardModalComponent);
    component = fixture.componentInstance;
    authService = TestBed.inject(AuthService);
    mediaService = TestBed.inject(MediaService);
    dataService = TestBed.inject(DataService);
    userService = TestBed.inject(UserService);
    productService = TestBed.inject(ProductService);
    component.product = mockProducts[0];
    component.user = mockUser;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should have a product from the @Input', () => {
    const testProduct = {
      name: 'test',
      description: 'test',
      price: 10,
      quantity: 10,
    };
    component.product = testProduct;
    fixture.detectChanges();
    expect(component.product).toEqual(testProduct);
  });

  it('should initialize user data and product thumbnail in ngOnInit', () => {
    const mediaService = TestBed.inject(MediaService);

    const prodcutMediaSpy = spyOn(mediaService, 'getProductMedia').and
      .returnValue(
        of(mockMediaResponse),
      );

    component.ngAfterViewInit();

    expect(prodcutMediaSpy).toHaveBeenCalled();

    expect(component.user).toEqual(mockUser);

    expect(component.picture).toEqual(component.placeholder);
  });

  it('should subscribe to DataService and update properties in ngOnInit', () => {
    const dataService = TestBed.inject(DataService);
    const mediaService = TestBed.inject(MediaService);
    const userService = TestBed.inject(UserService);
    const mockProductId = '123';

    const deleteImage$Spy = jasmine.createSpyObj('deleteImage$', ['pipe']);
    deleteImage$Spy.pipe.and.returnValue(of(1));

    const ids$Spy = jasmine.createSpyObj('ids$', ['pipe']);
    ids$Spy.pipe.and.returnValue(of(mockProductId));

    dataService['deleteImage$'] = deleteImage$Spy;
    dataService['ids$'] = ids$Spy;

    spyOn(mediaService, 'getProductMedia').and.returnValue(
      of(mockMediaResponse),
    );

    const getOwnerInfoSpy = spyOn(userService, 'getOwnerInfo').and.returnValue(
      of(mockUser),
    );

    component.ngOnInit();

    expect(getOwnerInfoSpy).toHaveBeenCalled();
    expect(component.currentDeleteIndex).toBe(1);

    expect(component.images).toEqual(
      mockMediaResponse.media.map((item) =>
        mediaService.formatMultipleMedia(item)
      ),
    );
    expect(component.imageIds).toEqual(
      mockMediaResponse.media.map((item) => item.id),
    );

    expect(component.owner).toEqual(mockUser);
  });

  it('should increment currentImageIndex when calling nextImage()', () => {
    component.currentImageIndex = 0;
    component.images = ['image1', 'image2', 'image3'];

    component.nextImage();

    expect(component.currentImageIndex).toBe(1);
  });

  it('should wrap around currentImageIndex when calling nextImage()', () => {
    component.currentImageIndex = 2;
    component.images = ['image1', 'image2', 'image3'];

    component.nextImage();

    expect(component.currentImageIndex).toBe(0);
  });

  it('should decrement currentImageIndex when calling prevImage()', () => {
    component.currentImageIndex = 2;
    component.images = ['image1', 'image2', 'image3'];

    component.prevImage();

    expect(component.currentImageIndex).toBe(1);
  });

  it('should wrap around currentImageIndex when calling prevImage()', () => {
    component.currentImageIndex = 0;
    component.images = ['image1', 'image2', 'image3'];

    component.prevImage();

    expect(component.currentImageIndex).toBe(0);
  });

  it('should delete an image by index and update related properties', () => {
    const mediaService = TestBed.inject(MediaService);
    component.tabGroup = {
      selectedIndex: 0,
      //eslint-disable-next-line
    } as any;
    component.images = ['image1', 'image2', 'image3'];
    component.imageIds = ['id1', 'id2', 'id3'];
    component.currentImageIndex = 1;

    spyOn(mediaService, 'deleteProductImage');

    component.deleteImage(1);

    expect(component.images).toEqual(['image1', 'image3']);
    expect(component.imageIds).toEqual(['id1', 'id3']);
    expect(component.currentImageIndex).toBe(0);
    expect(mediaService.deleteProductImage).toHaveBeenCalledWith(
      'id2',
    );
  });

  it('should set form values based on product', () => {
    component.productForm = new FormGroup({
      price: new FormControl(null),
      quantity: new FormControl(null),
    });

    component.initFormValues();

    expect(component.productForm.get('price')?.value).toBe(12.99);
    expect(component.productForm.get('quantity')?.value).toBe(5);
  });

  it('should close the modal and set formOpen to false', () => {
    const formStateService = TestBed.inject(FormStateService);
    const formOpenSpy = spyOn(formStateService, 'setFormOpen');
    const confirmSpy = spyOn(component, 'closeConfirm');

    component.hideModal();

    expect(formOpenSpy).toHaveBeenCalled();
    expect(confirmSpy).toHaveBeenCalled();
  });

  it('should update formValid property', () => {
    component.productForm = new FormGroup({
      price: new FormControl(10),
      quantity: new FormControl(5),
    });

    component.onValidate();

    expect(component.formValid).toBe(true);
  });

  it('should convert File to Blob', () => {
    const file = new File(['file contents'], 'example.txt', {
      type: 'text/plain',
    });

    const blob = component.fileToBlob(file);

    expect(blob.size).toBe(13);
    expect(blob.type).toBe('text/plain');
  });

  it('should set fileSelected and imageValid when a file is selected', () => {
    const event = {
      files: [
        new File(['file contents'], 'example.txt', { type: 'text/plain' }),
      ] as File[],
    } as FileSelectEvent;

    component.onFileSelected(event);

    expect(component.fileSelected).toBeDefined();
    expect(component.imageValid).toBe(true);
    expect(component.filename).toBe('example.txt');
  });

  it('should reset fileSelected and set imageValid to false when no file is selected', () => {
    const event = { files: [] as File[] } as FileSelectEvent;

    component.onFileSelected(event);

    expect(component.fileSelected).toBeNull();
    expect(component.imageValid).toBe(false);
    expect(component.filename).toBe('');
  });

  it('should submit product and update productAdded when form is valid', () => {
    component.initFormValues();
    component.tabGroup = {
      selectedIndex: 0,
      //eslint-disable-next-line
    } as any;
    const mediaForm = new FormData();
    component.productForm.get('name')?.setValue('test2');
    component.productForm.get('quantity')?.setValue(51);
    component.productForm.get('description')?.setValue('test desc');
    component.productForm.get('price')?.setValue(12.99);
    const formValue: ProductRequest = {
      name: 'test2',
      quantity: 51,
      description: 'test desc',
      price: 12.99,
    };

    const addProductSpy = spyOn(productService, 'addProduct').and.returnValue(
      of(mockProducts[0]),
    );

    const updateProductSpy = spyOn(productService, 'updateProductAdded');

    component.formValid = true;
    component.fileSelected = new File(['file contents'], 'example.txt', {
      type: 'text/plain',
    });

    component.submitProduct();

    expect(component.formValid).toBeTrue();
    expect(addProductSpy).toHaveBeenCalledWith(
      formValue,
      mediaForm,
    );
    expect(updateProductSpy).toHaveBeenCalledWith(
      mockProducts[0],
    );
  });

  it('should send deleteProduct request to product service', () => {
    const deleteProductSpy = spyOn(productService, 'deleteProduct');
    component.deleteProduct('123');
    expect(deleteProductSpy).toHaveBeenCalledWith('123');
  });

  it('should set confirm and deleteConfirm to false', () => {
    component.closeConfirm('tag');
    expect(component.confirm).toBe(false);
    expect(component.imageDeleteConfirm).toBe(false);
  });

  it('should open imageDelete confirm', () => {
    component.openConfirm('');
    expect(component.imageDeleteConfirm).toBe(true);
  });

  it('should open confirm', () => {
    component.openConfirm('product');
    expect(component.confirm).toBe(true);
  });
});
