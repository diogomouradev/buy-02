import { ComponentFixture, TestBed } from '@angular/core/testing';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';

import { AddProductComponent } from './add-product.component';
import { ProductService } from '../service/product.service';
import { HttpClientModule } from '@angular/common/http';
import { FormGroup, ReactiveFormsModule } from '@angular/forms';
import { FileSelectEvent, FileUploadModule } from 'primeng/fileupload';
import { InputTextareaModule } from 'primeng/inputtextarea';
import { InputNumberModule } from 'primeng/inputnumber';
import { of } from 'rxjs';
import { environment } from 'src/environments/environment';
import { ProductRequest } from '../interfaces/product-request';
import { FormStateService } from '../service/form-state.service';

describe('AddProductComponent', () => {
  let component: AddProductComponent;
  let productService: ProductService;
  let fixture: ComponentFixture<AddProductComponent>;
  let httpMock: HttpTestingController;

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

  const mockProductRequest = {
    name: 'test',
    price: 12.99,
    quantity: 10,
    description: 'test desc',
  } as ProductRequest;

  beforeEach(() => {
    TestBed.configureTestingModule({
      declarations: [AddProductComponent],
      providers: [ProductService],
      imports: [
        HttpClientTestingModule,
        HttpClientModule,
        ReactiveFormsModule,
        FileUploadModule,
        InputTextareaModule,
        InputNumberModule,
      ],
    });
    fixture = TestBed.createComponent(AddProductComponent);
    productService = TestBed.inject(ProductService);
    httpMock = TestBed.inject(HttpTestingController);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize correctly', () => {
    expect(component).toBeTruthy();
    expect(component.productForm).toBeInstanceOf(FormGroup);
    expect(component.productForm.get('name')).toBeTruthy();
    expect(component.formValid).toBeFalse();
    expect(component.fileSelected).toBeNull();
    expect(component.filename).toBeNull();
    expect(component.requestSent).toBeFalse();
    expect(component.success).toBeFalse();
    expect(component.showProductForm).toBeFalse();
    expect(component.edit).toBeFalse();
  });

  it('should correctly validate the form', () => {
    component.productForm.setValue({
      name: 'Product Name',
      description: 'Product Description',
      price: 10,
      quantity: 5,
    });
    component.onValidate();
    expect(component.formValid).toBeTrue();

    component.productForm.setValue({
      name: 'Pr',
      description: 'Product Description',
      price: -1,
      quantity: 5,
    });
    component.onValidate();
    expect(component.formValid).toBeFalse();
  });

  it('should set fileSelected and filename when a file is selected', () => {
    const file = new File(['file contents'], 'example.txt', {
      type: 'text/plain',
    });
    component.onFileSelected({ files: [file] } as FileSelectEvent);

    expect(component.fileSelected).toEqual(file);
    expect(component.filename).toBe('example.txt');
  });

  it('should reset fileSelected and filename when no file is selected', () => {
    component.onFileSelected({ files: [] } as unknown as FileSelectEvent);

    expect(component.fileSelected).toBeNull();
    expect(component.filename).toBeNull();
  });

  it('should convert a File to a Blob', () => {
    const file = new File(['file contents'], 'example.txt', {
      type: 'text/plain',
    });
    const blob = component.fileToBlob(file);

    expect(blob).toBeInstanceOf(Blob);
    expect(blob.size).toBe(file.size);
    expect(blob.type).toBe(file.type);
  });

  it('should not submit product if the form is invalid', () => {
    const productService = TestBed.inject(ProductService);
    component.formValid = false;
    const addProductSpy = spyOn(productService, 'addProduct');

    component.submitProduct();

    expect(addProductSpy).not.toHaveBeenCalled();
  });

  it('should submit product and update productAdded if the form is valid', () => {
    const request = mockProductRequest;

    component.formValid = true;
    component.productForm.controls['name'].setValue(request.name);
    component.productForm.controls['description'].setValue(request.description);
    component.productForm.controls['price'].setValue(request.price);
    component.productForm.controls['quantity'].setValue(request.quantity);

    component.fileSelected = new File(['file contents'], 'example.txt', {
      type: 'text/plain',
    });
    const mediaReq = new FormData();
    mediaReq.append(
      'image',
      component.fileToBlob(component?.fileSelected),
      'example.txt,',
    );
    const closeModalSpy = spyOn(component, 'closeModal');
    const addProductSpy = spyOn(productService, 'addProduct').and.returnValue(
      of(mockProducts[0]),
    );
    const updateProductSpy = spyOn(productService, 'updateProductAdded');

    component.submitProduct();

    expect(addProductSpy).toHaveBeenCalledWith(
      request,
      mediaReq,
    );
    expect(updateProductSpy).toHaveBeenCalledWith(mockProducts[0]);
    expect(component.success).toBeTrue();
    expect(component.productResult).toEqual('Product added successfully');
    expect(closeModalSpy).toHaveBeenCalled();
  });

  it('should close the modal', () => {
    const formStateService = TestBed.inject(FormStateService);
    component.modalRef = document.createElement('dialog');
    const formStateServiceSpy = spyOn(formStateService, 'setFormOpen');
    const modalRefSpy = spyOn(component.modalRef, 'close');

    component.closeModal();

    expect(formStateServiceSpy).toHaveBeenCalledWith(false);
    expect(modalRefSpy).toHaveBeenCalled();
  });
});
