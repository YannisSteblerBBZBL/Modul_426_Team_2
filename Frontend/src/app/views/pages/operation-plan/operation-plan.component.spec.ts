import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OperationPlanComponent } from './operation-plan.component';

describe('OperationPlanComponent', () => {
  let component: OperationPlanComponent;
  let fixture: ComponentFixture<OperationPlanComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OperationPlanComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OperationPlanComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
