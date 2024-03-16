import { AggregatedProduct, Product } from './product';

export interface Order {
  id?: string;
  status: 'PENDING' | 'CANCELLED' | 'CONFIRMED';
  buyerId: string;
  sellerId: string;
  product: Product;
  orderPlacedAt?: Date;
  quantity: number;
}

export interface PersonalOrder {
  pending: Order[];
  confirmed: Order[];
  cancelled: Order[];
}

export interface CartItem {
  id?: string;
  status: 'PENDING' | 'CANCELLED' | 'CONFIRMED';
  buyerId: string;
  sellerId: string;
  product: Product;
  addedToCartAt?: Date;
  quantity: number;
}

export interface CartResponse {
  cart: Cart;
  processed: boolean;
  orderModifications: OrderModifications;
}

export interface OrderModifications {
  notes: Set<string>;
  modifications: Order[];
}

export interface OrderStatusUpdate {
  id: string;
  status: 'CANCELLED' | 'CONFIRMED';
}

export interface Cart {
  orders: CartItem[];
}

export type DashboardItem = Order | AggregatedProduct;
