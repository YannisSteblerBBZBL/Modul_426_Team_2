import { ComponentFixture, TestBed } from '@angular/core/testing';

import { HelperManagmentComponent } from './helper-managment.component';

describe('HelperManagmentComponent', () => {
  let component: HelperManagmentComponent;
  let fixture: ComponentFixture<HelperManagmentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HelperManagmentComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(HelperManagmentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
