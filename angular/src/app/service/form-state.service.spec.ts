import { TestBed } from '@angular/core/testing';

import { FormStateService } from './form-state.service';

describe('FormStateService', () => {
  let service: FormStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(FormStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should initialize formOpen$ to false', () => {
    service.formOpen$.subscribe((value) => {
      expect(value).toBe(false);
    });
  });

  it('should set formOpen$ to true', () => {
    service.setFormOpen(true);
    service.formOpen$.subscribe((value) => {
      expect(value).toBe(true);
    });
  });

  it('should set formOpen$ to false', () => {
    service.setFormOpen(false);
    service.formOpen$.subscribe((value) => {
      expect(value).toBe(false);
    });
  });
});
