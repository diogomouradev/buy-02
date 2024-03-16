import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  Input,
  OnInit,
  ViewChild,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormControl, FormGroup } from '@angular/forms';
import { MatTabGroup } from '@angular/material/tabs';
import { FileSelectEvent, FileUpload } from 'primeng/fileupload';
import { Media } from 'src/app/interfaces/media';
import { Product } from 'src/app/interfaces/product';
import { ProductRequest } from 'src/app/interfaces/product-request';
import { User } from 'src/app/interfaces/user';
import { DataService } from 'src/app/service/data.service';
import { FormStateService } from 'src/app/service/form-state.service';
import { MediaService } from 'src/app/service/media.service';
import { OrderService } from 'src/app/service/order.service';
import { ProductService } from 'src/app/service/product.service';
import { UserService } from 'src/app/service/user.service';
import { ValidatorService } from 'src/app/service/validator.service';
import { environment } from 'src/environments/environment';

@Component({
  selector: 'app-product-card-modal',
  templateUrl: './product-card-modal.component.html',
  styleUrls: ['./product-card-modal.component.css'],
})
export class ProductCardModalComponent implements OnInit, AfterViewInit {
  @ViewChild('tabGroup')
    tabGroup!: MatTabGroup;
  @ViewChild('productModal')
    productModal?: ElementRef;
  @ViewChild('imageUpload')
    imageUploadButton: FileUpload | undefined;
  @Input()
    dialog?: HTMLDialogElement;
  @Input()
    product!: Product;
  @Input()
    user?: User;

  price = 0;
  quantity = 0;
  currentImageIndex = 0;
  currentDeleteIndex = 0;
  maxQuantity: number = 1;
  orderQuantity = 1;
  formOpen = false;
  formValid = true;
  success = false;
  confirm = false;
  requestSent = false;
  imageValid = false;
  imageDeleteConfirm = false;
  deletingProduct = false;
  productResult: string = '';
  errorMessage: string = '';
  filename: string = '';
  images: string[] = [];
  imageIds: string[] = [];
  placeholder: string = environment.placeholder;
  picture: string = this.placeholder;
  fileSelected: File | null = null;
  owner: User = {} as User;
  currentUser: User = {} as User;

  private productService = inject(ProductService);
  private mediaService = inject(MediaService);
  private formStateService = inject(FormStateService);
  private userService = inject(UserService);
  private validatorService = inject(ValidatorService);
  private dataService = inject(DataService);
  private orderService = inject(OrderService);
  private destroyRef = inject(DestroyRef);
  private changeDetector = inject(ChangeDetectorRef);

  productForm: FormGroup = new FormGroup({
    name: new FormControl(null, [this.validatorService.productNameValidator()]),
    description: new FormControl(null, [
      this.validatorService.productDescriptionValidator(),
    ]),
    price: new FormControl(null, [
      this.validatorService.productPriceValidator(),
    ]),
    quantity: new FormControl(null, [
      this.validatorService.productQuantityValidator(),
    ]),
  });

  ngOnInit(): void {
    this.maxQuantity = this.product.quantity;
    this.initFormValues();

    this.dataService.deleteImage$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((index) => {
        this.currentDeleteIndex = index;
      });

    this.mediaService.imageAdded$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.getProductImages();
        this.getProductOwnerInfo();
        this.changeDetector.detectChanges();
      });

    this.dataService.ids$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (id) => {
        if (id !== this.product.id) return;
        this.getProductImages();
        this.getProductOwnerInfo();
        this.changeDetector.detectChanges();
      },
    );
  }

  ngAfterViewInit(): void {
    this.getProductImages();
    this.getProductOwnerInfo();
  }

  getProductOwnerInfo() {
    this.userService.getOwnerInfo(
      this.product.userId!,
    ).pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe({
        next: (user) => this.owner = user,
      });
  }

  getProductImages() {
    this.mediaService
      .getProductMedia(this.product.id!).pipe(
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (data) => {
          if (data?.media?.length > 0) {
            this.imageIds = [];
            this.images = data.media.map((item) => {
              this.imageIds.push(item.id);
              return this.mediaService.formatMultipleMedia(item);
            });
          } else {
            this.imageIds = [];
            this.images = [this.placeholder];
          }
          this.changeDetector.detectChanges();
        },
      });
  }

  openImageInNewTab(imageData: string): void {
    const newTab = window.open();
    if (newTab) {
      newTab.document.write(
        `<img src="${imageData}" alt="product image" />`,
      );
    }
  }

  nextImage() {
    this.currentImageIndex = (this.currentImageIndex + 1) % this.images.length;
    this.dataService.changeDeleteIndex(this.currentImageIndex);
  }

  prevImage() {
    this.currentImageIndex = Math.max(
      (this.currentImageIndex - 1) % this.images.length,
      0,
    );
    this.dataService.changeDeleteIndex(this.currentImageIndex);
  }

  deleteImage(index: number) {
    if (this.imageIds.length === 1) index = 0;
    const id = this.imageIds[index];
    this.mediaService.deleteProductImage(id);
    this.mediaService.updateImageAdded({} as Media);
    this.currentImageIndex--;
    this.dataService.sendProductId(this.product.id!);
    this.tabGroup.selectedIndex = 0;
    if (this.currentImageIndex < 0) this.currentImageIndex = 0;
    if (this.images.length === 0) this.picture = this.placeholder;
    this.images.splice(index, 1);
    this.imageIds.splice(index, 1);
    this.productService.updateProductAdded(this.product);
    this.closeConfirm('image');
    this.getProductImages();
    this.changeDetector.detectChanges();
  }

  handleEscDown(event: KeyboardEvent) {
    if (event.key === 'Escape') {
      this.hideModal();
    }
  }

  addToCart() {
    if (!this.orderQuantity) return;
    if (this.product.quantity < this.orderQuantity) {
      this.orderQuantity = this.product.quantity;
    }
    this.orderService.addToCart(this.product, this.orderQuantity);
  }

  isAmountValid() {
    return this.product.quantity < 0 ||
      this.orderQuantity > this.product.quantity || this.orderQuantity <= 0;
  }

  initFormValues() {
    this.productForm.get('price')?.setValue(this.product.price);
    this.productForm.get('quantity')?.setValue(this.product.quantity);
    this.productForm.get('name')?.setValue(this.product.name);
    this.productForm.get('description')?.setValue(this.product.description);
  }

  hideModal() {
    if (!this.deletingProduct) {
      this.formStateService.setFormOpen(false);
      this.dialog?.close();
      this.closeConfirm('product');
    }
  }

  onValidate() {
    this.formValid = this.productForm.valid;
  }

  fileToBlob(file: File): Blob {
    const blob = new Blob([file], { type: file.type });
    return blob;
  }

  onFileSelected(event: FileSelectEvent) {
    const input = event.files[0];
    if (input) {
      this.filename = input.name;
      this.fileSelected = input;
      this.imageValid = true;
    } else {
      this.fileSelected = null;
      this.imageValid = false;
    }
  }

  updateProduct() {
    if (this.formValid) {
      const prod = {
        name: this.productForm.value.name,
        description: this.productForm.value.description,
        price: this.productForm.value.price,
        quantity: this.productForm.value.quantity,
      } as Product;
      this.productService.updateProduct(this.product.id!, prod).pipe(
        takeUntilDestroyed(this.destroyRef),
      ).subscribe({
        next: (data) => {
          this.productService.updateProductAdded(data!);
        },
      });
      this.tabGroup.selectedIndex = 0;
    }
  }

  submitProduct() {
    let mediaData;
    if (this.fileSelected) {
      mediaData = new FormData();
      mediaData.append(
        'image',
        this.fileToBlob(this.fileSelected),
        this.filename,
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
        },
        error: () => {
          this.success = false;
          this.productResult = 'Error adding product';
        },
      });
    }
    this.productForm.reset();
    this.imageUploadButton?.clear();
    this.tabGroup.selectedIndex = 0;
  }

  submitImage() {
    if (!this.fileSelected) return;
    const mediaForm = new FormData();
    mediaForm.append(
      'image',
      this.fileToBlob(this.fileSelected),
      this.filename,
    );
    this.mediaService.addMedia(
      this.product.id!,
      mediaForm,
    ).pipe(takeUntilDestroyed(this.destroyRef)).subscribe({
      next: (data) => {
        this.images.push(this.mediaService.formatMultipleMedia(data));
        this.mediaService.updateImageAdded(
          this.mediaService.formatMultipleMedia(data),
        );
        this.fileSelected = null;
        this.filename = '';
        this.dataService.sendProductId(this.product.id!);
        this.getProductImages();
      },
      error: (err) => this.errorMessage = err.error.message,
    });
    this.tabGroup.selectedIndex = 0;
    this.dataService.sendProductId(this.product.id!);
  }

  deleteProduct(productId: string): void {
    this.deletingProduct = true;
    this.productService.deleteProduct(productId);
    this.dialog?.close();
    this.formStateService.setFormOpen(false);
  }

  openConfirm(form: string) {
    if (form === 'product') {
      this.confirm = true;
    } else this.imageDeleteConfirm = true;
  }

  closeConfirm(tag: string) {
    if (tag === 'image') {
      this.imageDeleteConfirm = false;
    } else {
      this.confirm = false;
    }
  }
}
