import { Product } from './product';

export interface ProductCreationResponse {
  product: Product;
  errors: string[];
}
