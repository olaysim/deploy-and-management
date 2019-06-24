import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributedControlComponent } from './distributed-control.component';

describe('DistributedControlComponent', () => {
  let component: DistributedControlComponent;
  let fixture: ComponentFixture<DistributedControlComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributedControlComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributedControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
