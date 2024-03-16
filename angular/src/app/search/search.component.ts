import { Component, DestroyRef, inject, OnInit } from '@angular/core';
import { Observable, of } from 'rxjs';
import { Product } from '../interfaces/product';
import { User } from '../interfaces/user';
import { ProductService } from '../service/product.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { StateService } from '../service/state.service';

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css'],
})
export class SearchComponent implements OnInit {
  products$: Observable<Product[]> | null = null;
  products: Product[] = [];
  currentUser: User = {} as User;
  searchText: string = '';
  maxPrice: string = '';
  minPrice: string = '';

  private productService = inject(ProductService);
  private destroyRef = inject(DestroyRef);
  private stateService = inject(StateService);

  ngOnInit(): void {
    this.stateService.getStateAsObservable().pipe(
      takeUntilDestroyed(this.destroyRef),
    )
      .subscribe((user) => this.currentUser = user);

    this.productService.getProducts().pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe((products) => {
        this.products$ = of(products.reverse()); //NOSONAR
        this.products = products;
      });
  }

  search() {
    this.products$ = of(this.products.filter((e) => this.searchFilter(e)));
  }

  private searchFilter(product: Product): boolean {
    const words = this.searchText.trim().toLowerCase().split(' ');

    const textMatches = words.every((word) =>
      product.name.toLowerCase().includes(word) ||
      product.description.toLowerCase().includes(word)
    );

    const priceInRange = (!this.minPrice || product.price >= +this.minPrice) &&
      (!this.maxPrice || product.price <= +this.maxPrice);

    return (textMatches && priceInRange);
  }

  reset() {
    this.searchText = '';
    this.maxPrice = '';
    this.minPrice = '';
    this.search();
  }
}
