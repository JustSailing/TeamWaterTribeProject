import { TestBed } from '@angular/core/testing';

import { SubTodoService } from './subtodo.service';

describe('Subtodo', () => {
  let service: SubTodoService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(SubTodoService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
