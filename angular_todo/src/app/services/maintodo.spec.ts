import { TestBed } from '@angular/core/testing';

import { MainTodoService } from './maintodo.service';

describe('Maintodo', () => {
  let service: MainTodoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(MainTodoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
