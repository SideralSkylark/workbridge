import { ComponentFixture, TestBed } from '@angular/core/testing';

import { AproveProvidersComponent } from './aprove-providers.component';

describe('AproveProvidersComponent', () => {
  let component: AproveProvidersComponent;
  let fixture: ComponentFixture<AproveProvidersComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AproveProvidersComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(AproveProvidersComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
