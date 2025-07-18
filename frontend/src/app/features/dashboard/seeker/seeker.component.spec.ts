import { ComponentFixture, TestBed } from '@angular/core/testing';

import { ServiceRequestsComponent } from './seeker.component';

describe('ServiceRequestsComponent', () => {
  let component: ServiceRequestsComponent;
  let fixture: ComponentFixture<ServiceRequestsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [ServiceRequestsComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ServiceRequestsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
