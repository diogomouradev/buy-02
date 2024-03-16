import {
  AfterViewInit,
  ChangeDetectorRef,
  Component,
  DestroyRef,
  ElementRef,
  inject,
  Input,
  OnChanges,
  ViewChild,
} from '@angular/core';
import { BehaviorSubject, of } from 'rxjs';
import { Product } from 'src/app/interfaces/product';
import { MediaService } from 'src/app/service/media.service';
import { User } from 'src/app/interfaces/user';
import { FormStateService } from 'src/app/service/form-state.service';
import { environment } from 'src/environments/environment';
import { DataService } from 'src/app/service/data.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-product-card',
  templateUrl: './product-card.component.html',
  styleUrls: ['./product-card.component.css'],
})
export class ProductCardComponent implements AfterViewInit, OnChanges {
  @ViewChild('container')
    container: ElementRef | undefined;
  @ViewChild('productModal')
    productModal: ElementRef | undefined;
  @Input()
    product: Product = {} as Product;
  @Input()
    currentUser: User = {} as User;

  placeholder: string = environment.placeholder;
  imageSrc$: BehaviorSubject<string> = new BehaviorSubject<string>(
    this.placeholder,
  );
  modalVisible = false;

  private mediaService = inject(MediaService);
  private formStateService = inject(FormStateService);
  private dataService = inject(DataService);
  private destroyRef = inject(DestroyRef);
  private changeDetectorRef = inject(ChangeDetectorRef);

  ngAfterViewInit(): void {
    this.formStateService.formOpen$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((isOpen) => {
        if (isOpen) {
          this.container?.nativeElement.classList.add('blur-filter');
        } else {
          this.container?.nativeElement.classList.remove('blur-filter');
        }
      });

    this.dataService.ids$.pipe(takeUntilDestroyed(this.destroyRef)).subscribe(
      (id) => {
        this.updateThumbnailIfEmpty(id);
      },
    );

    this.mediaService.imageAdded$.pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.getProductThumbnail();
        this.changeDetectorRef.detectChanges();
      });
  }

  ngOnChanges(): void {
    this.getProductThumbnail();
  }

  private updateThumbnailIfEmpty(id: string) {
    if (id === this.product.id) {
      this.getProductThumbnail();
    }
  }

  private getProductThumbnail() {
    this.mediaService
      .getProductThumbnail(this.product.id!).pipe(
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe({
        next: (media) => {
          if (media?.image) {
            this.imageSrc$.next(this.mediaService.formatMedia(media));
            this.changeDetectorRef.detectChanges();
          } else {
            this.imageSrc$.next(this.placeholder);
            this.changeDetectorRef.detectChanges();
          }
        },
        error: (err) => {
          if (err.status === 404) return of(null);
          return of(null);
        },
      });
  }

  showModal() {
    this.modalVisible = true;
    setTimeout(() => this.productModal?.nativeElement.show(), 100);
    this.dataService.sendProductId(this.product.id!);
    this.formStateService.setFormOpen(true);
    this.modalVisible = true;
  }

  hideModal() {
    this.formStateService.setFormOpen(false);
    this.productModal?.nativeElement.close();
    this.modalVisible = false;
  }
}
