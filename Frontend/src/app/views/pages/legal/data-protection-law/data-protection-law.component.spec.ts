import { ComponentFixture, TestBed } from '@angular/core/testing';

import { DataProtectionLawComponent } from './data-protection-law.component';

describe('DataProtectionLawComponent', () => {
  let component: DataProtectionLawComponent;
  let fixture: ComponentFixture<DataProtectionLawComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [DataProtectionLawComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(DataProtectionLawComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
