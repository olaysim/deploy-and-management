import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributedInfoComponent } from './distributed-info.component';

describe('DistributedInfoComponent', () => {
  let component: DistributedInfoComponent;
  let fixture: ComponentFixture<DistributedInfoComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributedInfoComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributedInfoComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
