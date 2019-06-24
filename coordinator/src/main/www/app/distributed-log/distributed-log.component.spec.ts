import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributedLogComponent } from './distributed-log.component';

describe('DistributedLogComponent', () => {
  let component: DistributedLogComponent;
  let fixture: ComponentFixture<DistributedLogComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributedLogComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributedLogComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
