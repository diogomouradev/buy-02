export interface Media {
  id: string;
  //eslint-disable-next-line
  image: any;
  productId: string;
  userId: string;
  mimeType: string;
}

export interface MediaResponse {
  productId: string;
  media: Media[];
}
