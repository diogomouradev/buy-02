import { HttpClient, HttpHeaders } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { environment } from 'src/environments/environment';
import { Media, MediaResponse } from '../interfaces/media';
import { BehaviorSubject, Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class MediaService {
  placeholder: string = environment.placeholder;

  private avatarSource = new BehaviorSubject<string>(this.placeholder);
  avatar$ = this.avatarSource.asObservable();

  private imageAddedSource = new Subject<Media>();
  imageAdded$ = this.imageAddedSource.asObservable();

  private http = inject(HttpClient);

  //eslint-disable-next-line
  updateImageAdded(data: any): void {
    this.imageAddedSource.next(data);
  }

  //eslint-disable-next-line
  updateAvatar(data: any): void {
    this.avatarSource.next(data);
  }

  getProductThumbnail(productId: string): Observable<Media> {
    const address = environment.productMediaURL + productId;
    return this.http.get<Media>(address, { withCredentials: true });
  }

  getProductMedia(productId: string): Observable<MediaResponse> {
    const address = environment.mediaURL + '/' + productId;
    return this.http.get<MediaResponse>(address, { withCredentials: true });
  }

  getAvatar(userId: string): Observable<Media> {
    const address = environment.userMediaURL + userId;
    return this.http.get<Media>(address, { withCredentials: true });
  }

  addMedia(id: string, image: FormData): Observable<Media> {
    const address = environment.mediaURL;
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data');
    return this.http
      .post<Media>(address, image, {
        params: { productId: id },
        headers: headers,
        withCredentials: true,
      });
  }
  //eslint-disable-next-line
  deleteAvatar(userId: string): any {
    const address = environment.mediaURL;
    this.avatarSource.next(this.placeholder);
    return this.http.delete(address, {
      params: { userId: userId },
      withCredentials: true,
    });
  }

  deleteProductImage(mediaId: string): void {
    const address = environment.mediaURL + '/' + mediaId;
    this.http.delete(address, { withCredentials: true }).subscribe(() => 0);
  }

  uploadAvatar(userId: string, image: FormData): Observable<Media> {
    const address = environment.mediaURL;
    const headers = new HttpHeaders();
    headers.append('Content-Type', 'multipart/form-data');
    return this.http
      .post<Media>(address, image, {
        params: { userId: userId },
        headers: headers,
        withCredentials: true,
      });
  }

  formatMedia(media: Media): string {
    if (!media.image) return this.placeholder;
    return 'data:' + media.mimeType + ';base64,' + media.image;
  }

  formatMultipleMedia(media: Media): string {
    if (!media.image?.data) return this.placeholder;
    return 'data:' + media.mimeType + ';base64,' + media.image.data;
  }
}
