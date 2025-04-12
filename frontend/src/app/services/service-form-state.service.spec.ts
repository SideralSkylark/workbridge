import { TestBed } from '@angular/core/testing';

import { ServiceFormStateService } from './service-form-state.service';

describe('ServiceFormStateService', () => {
  let service: ServiceFormStateService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ServiceFormStateService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
