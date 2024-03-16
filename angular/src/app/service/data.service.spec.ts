import { TestBed } from '@angular/core/testing';
import { DataService } from './data.service';

describe('DataService', () => {
  let service: DataService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(DataService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should send and receive data', () => {
    const testData = 'Test data';

    service.sendData(testData);
    service.data$.subscribe((data) => {
      expect(data).toBe(testData);
    });
  });

  it('should send and receive product IDs', () => {
    const testId = '12345';
    const sendIdSpy = spyOn(service, 'sendProductId').and.callThrough();

    service.sendProductId(testId);
    service.ids$.subscribe((id) => {
      expect(id).toBe(testId);
    });
    expect(sendIdSpy).toHaveBeenCalled();
  });

  it('should change the delete image index', () => {
    const testIndex = 5;

    service.changeDeleteIndex(testIndex);
    service.deleteImage$.subscribe((index) => {
      expect(index).toBe(testIndex);
    });
  });
});
