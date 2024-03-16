import { TestBed } from '@angular/core/testing';

import { MediaService } from './media.service';
import {
  HttpClientTestingModule,
  HttpTestingController,
} from '@angular/common/http/testing';
import { environment } from 'src/environments/environment';
import { Media } from '../interfaces/media';

describe('MediaService', () => {
  let service: MediaService;
  let httpTestingController: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [MediaService],
    });
    service = TestBed.inject(MediaService);
    httpTestingController = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpTestingController.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should update imageAddedSource', () => {
    const mockMedia = {
      id: '1',
      image: 'testimage',
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };

    const spy = spyOn(service, 'updateImageAdded').and.callThrough();

    service.updateImageAdded(mockMedia);
    service.imageAdded$.subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });
    expect(spy).toHaveBeenCalled();
  });

  it('should return product thumbnail', () => {
    const prodId = '123';
    const mockMedia = {
      id: '123',
      image: 'testimage',
      productId: '1',
      userId: '',
      mimeType: 'image/jpeg',
    };
    service.getProductThumbnail(prodId).subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });
    const req = httpTestingController.expectOne(
      `${environment.productMediaURL}${prodId}`,
    );

    expect(req.request.method).toEqual('GET');
    req.flush(mockMedia);
  });

  it('should get product media', () => {
    const prodId = '123';
    const mockMediaResponse = {
      productId: '123',
      media: [
        {
          id: '1',
          image: 'testimage',
          productId: '123',
          userId: '',
          mimeType: 'image/jpeg',
        },
        {
          id: '2',
          image: 'testimage2',
          productId: '123',
          userId: '',
          mimeType: 'image/jpeg',
        },
      ],
    };
    service.getProductMedia(prodId).subscribe((media) => {
      expect(media).toEqual(mockMediaResponse);
    });
    const req = httpTestingController.expectOne(
      `${environment.mediaURL}/${prodId}`,
    );
    expect(req.request.method).toEqual('GET');
    req.flush(mockMediaResponse);
  });

  it('should return user avatar', () => {
    const userId = '123';
    const mockMedia = {
      id: '1',
      image: 'testimage',
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };
    service.getAvatar(userId).subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });
    const req = httpTestingController.expectOne(
      `${environment.userMediaURL}${userId}`,
    );

    expect(req.request.method).toEqual('GET');
    req.flush(mockMedia);
  });

  it('formatMedia should format valid media', () => {
    const mockMedia = {
      id: '1',
      image: 'testimage',
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };
    const formatted = service.formatMedia(mockMedia);
    expect(formatted).toEqual(
      'data:' + mockMedia.mimeType + ';base64,' + mockMedia.image,
    );
  });

  it('formatMedia should return placeholder if no media', () => {
    const mockMedia = {} as Media;
    const formatted = service.formatMedia(mockMedia);
    expect(formatted).toEqual(environment.placeholder);
  });

  it('formatMultipleMedia should format valid media', () => {
    const mockMedia = {
      id: '1',
      image: { data: 'testimage' },
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };
    const formatted = service.formatMultipleMedia(mockMedia);
    expect(formatted).toEqual(
      'data:' + mockMedia.mimeType + ';base64,' + mockMedia.image.data,
    );
  });

  it('formatMedia should return placeholder if no media', () => {
    const mockMedia = {} as Media;
    const formatted = service.formatMultipleMedia(mockMedia);
    expect(formatted).toEqual(environment.placeholder);
  });

  it('should add media', () => {
    const id = '123';
    const mockImage = new FormData();
    mockImage.append('image', new Blob(['sample image data']));

    const mockMedia: Media = {
      id: '1',
      image: 'testimage',
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };

    service.addMedia(id, mockImage).subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });

    const req = httpTestingController.expectOne(
      `${environment.mediaURL}?productId=${id}`,
    );

    expect(req.request.method).toEqual('POST');
    expect(req.request.params.get('productId')).toEqual(id);
    req.flush(mockMedia);
  });

  it('should delete avatar', () => {
    const userId = '123';

    service.deleteAvatar(userId).subscribe();

    const req = httpTestingController.expectOne(
      `${environment.mediaURL}?userId=${userId}`,
    );

    expect(req.request.method).toEqual('DELETE');
    req.flush({});
  });

  it('should delete product image', () => {
    const prodId = '123';

    service.deleteProductImage(prodId);

    const req = httpTestingController.expectOne(
      `${environment.mediaURL}/${prodId}`,
    );

    expect(req.request.method).toEqual('DELETE');
    req.flush({});
  });

  it('should upload avatar', () => {
    const userId = '123';
    const mockImage = new FormData();
    mockImage.append('image', new Blob(['sample image data']));

    const mockMedia: Media = {
      id: '1',
      image: 'testimage',
      productId: '',
      userId: '123',
      mimeType: 'image/jpeg',
    };

    service.uploadAvatar(userId, mockImage).subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });

    const req = httpTestingController.expectOne(
      `${environment.mediaURL}?userId=${userId}`,
    );

    expect(req.request.method).toEqual('POST');
    expect(req.request.params.get('userId')).toEqual(userId);
    req.flush(mockMedia);

    service.imageAdded$.subscribe((media) => {
      expect(media).toEqual(mockMedia);
    });
  });
});
