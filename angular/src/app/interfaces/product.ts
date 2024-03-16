export interface Product {
  name: string;
  quantity: number;
  description: string;
  price: number;
  userId?: string;
  id?: string;
  thumbnail?: string;
  image?: string;
}

export interface AggregatedProduct {
  product: Product;
  totalPrice: number;
  totalQuantity: number;
}
