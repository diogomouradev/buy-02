import {
  Component,
  DestroyRef,
  ElementRef,
  inject,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';
import { ProductService } from '../service/product.service';
import { ProductRequest } from '../interfaces/product-request';
import { FormStateService } from '../service/form-state.service';
import { Product } from '../interfaces/product';
import { ValidatorService } from '../service/validator.service';
import { FileSelectEvent } from 'primeng/fileupload';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-add-product',
  templateUrl: './add-product.component.html',
  styleUrls: ['./add-product.component.css'],
})
export class AddProductComponent implements OnInit {
  @Input()
    product?: Product;
  @Input('modalRef')
    modalRef?: HTMLDialogElement;
  @ViewChild('imageUpload')
    imageUpload?: ElementRef;
  @Input()
    dialog?: HTMLDialogElement;

  formValid = false;
  fileSelected: File | null = null;
  filename: string | null = null;
  requestSent = false;
  productResult: string = '';
  success = false;
  showProductForm = false;
  edit: boolean = this.product !== undefined;

  private productService = inject(ProductService);
  private formStateService = inject(FormStateService);
  private validatorService = inject(ValidatorService);
  private destroyRef = inject(DestroyRef);

  ngOnInit(): void {
    if (this.dialog) {
      this.dialog.show();
      this.modalRef?.show();
      this.showProductForm = true;
      this.formStateService.setFormOpen(true);
    }
  }

  productForm: FormGroup = new FormGroup({
    name: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(50),
    ]),
    description: new FormControl('', [
      Validators.required,
      Validators.minLength(4),
      Validators.maxLength(300),
    ]),
    price: new FormControl(0, [
      Validators.required,
      Validators.min(0),
      Validators.max(9999999999),
      this.validatorService.numberValidator(),
    ]),
    quantity: new FormControl(0, [
      Validators.required,
      Validators.min(0),
      Validators.max(99999),
      this.validatorService.numberValidator(),
    ]),
  });

  onFileSelected(event: FileSelectEvent) {
    const input = event.files[0];
    if (input) {
      this.filename = input.name;
      this.fileSelected = input;
    } else {
      this.fileSelected = null;
    }
  }

  fileToBlob(file: File): Blob {
    const blob = new Blob([file], { type: file.type });
    return blob;
  }

  onValidate() {
    this.formValid = this.productForm.valid;
  }

  submitProduct() {
    let mediaData;
    if (this.fileSelected) {
      mediaData = new FormData();
      mediaData.append(
        'image',
        this.fileToBlob(this.fileSelected),
        this.filename as string,
      );
    } else {
      mediaData = null;
    }

    let productRequest: ProductRequest;
    if (this.formValid) {
      productRequest = {
        name: this.productForm.value.name,
        description: this.productForm.value.description,
        price: this.productForm.value.price,
        quantity: this.productForm.value.quantity,
      } as ProductRequest;
      this.productService.addProduct(productRequest, mediaData).pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (data: Product | null) => {
          this.success = data !== null;
          this.requestSent = true;
          this.productResult = 'Product added successfully';
          this.productService.updateProductAdded(data!);
          this.closeModal();
        },
        error: () => {
          this.success = false;
          this.productResult = 'Error adding product';
        },
      });
    }
    this.productForm.reset();
  }

  closeModal() {
    this.formStateService.setFormOpen(false);
    this.modalRef?.close();
  }
}
