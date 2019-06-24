import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { DistributedUploadComponent } from './distributed-upload.component';

describe('DistributedUploadComponent', () => {
  let component: DistributedUploadComponent;
  let fixture: ComponentFixture<DistributedUploadComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ DistributedUploadComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DistributedUploadComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
