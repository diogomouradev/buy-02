import { HttpClient } from '@angular/common/http';
import { DestroyRef, inject, Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Product } from '../interfaces/product';
import { catchError, map, Observable, of, Subject, switchMap } from 'rxjs';
import { User } from '../interfaces/user';
import { ProductRequest } from '../interfaces/product-request';
import { ProductCreationResponse } from '../interfaces/product-creation-response';
import { AuthService } from './auth.service';
import { MediaService } from './media.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Media } from '../interfaces/media';

@Injectable({
  providedIn: 'root',
})
export class ProductService {
  private productAddedSource = new Subject<Product>();
  productAdded$ = this.productAddedSource.asObservable();

  private userProductsSource = new Subject<Product[]>();
  userProducts$ = this.userProductsSource.asObservable();

  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private mediaService = inject(MediaService);
  private destroyRef = inject(DestroyRef);

  updateProductAdded(product: Product): void {
    this.productAddedSource.next(product);
  }

  updateUserProducts(products: Product[]): void {
    this.userProductsSource.next(products);
  }

  getProducts(): Observable<Product[]> {
    const address = environment.productsURL;
    return this.http.get<Product[]>(address, { withCredentials: true });
  }

  getProductsById(userId: string): Observable<Product[]> {
    const address = environment.userProductsURL + userId;
    return this.http.get<Product[]>(address, { withCredentials: true });
  }

  getOwner(userId: string): Observable<User> {
    const address = environment.productsURL;
    return this.http.get<User>(address, {
      params: { userId: userId },
      withCredentials: true,
    });
  }

  getOwnerProducts() {
    this.userProducts$ = this.authService
      .getAuth()
      .pipe(switchMap((user) => this.getProductsById(user.id)));
  }

  updateProduct(
    id: string,
    form: ProductRequest,
  ): Observable<Product | null> {
    const address = environment.productsURL + '/' + id;
    return this.http.put<Product>(address, form, { withCredentials: true });
  }

  addProduct(
    form: ProductRequest,
    mediaForm: FormData | null,
  ): Observable<Product | null> {
    const address = environment.productsURL;
    return this.http
      .post<ProductCreationResponse>(address, form, { withCredentials: true })
      .pipe(
        map((data: ProductCreationResponse) => {
          if (mediaForm && mediaForm?.get('image') !== null) {
            this.mediaService.addMedia(data.product.id!, mediaForm).pipe(
              takeUntilDestroyed(this.destroyRef),
            ).subscribe({
              next: () => this.mediaService.updateImageAdded({} as Media),
            });
            mediaForm?.append('name', '');
            return data.product;
          }
          return data.product;
        }),
        catchError(() => of(null)),
      );
  }

  deleteProduct(id: string): void {
    const address = environment.productsURL + '/' + id;
    this.http.delete(address, { withCredentials: true }).pipe(
      takeUntilDestroyed(this.destroyRef),
    ).subscribe({
      next: () => {
        this.updateProductAdded({} as Product);
      },
    });
  }
}
