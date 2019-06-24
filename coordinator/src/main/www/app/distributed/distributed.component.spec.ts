import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributedComponent } from './distributed.component';

describe('DistributedComponent', () => {
  let component: DistributedComponent;
  let fixture: ComponentFixture<DistributedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
