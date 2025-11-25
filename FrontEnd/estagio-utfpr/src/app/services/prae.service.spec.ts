import { TestBed } from '@angular/core/testing';

import { PraeService } from './prae.service';

describe('PraeService', () => {
  let service: PraeService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(PraeService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
