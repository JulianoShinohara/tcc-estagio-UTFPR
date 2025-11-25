import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PraeComponent } from './prae.component';

describe('PraeComponent', () => {
  let component: PraeComponent;
  let fixture: ComponentFixture<PraeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PraeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PraeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
