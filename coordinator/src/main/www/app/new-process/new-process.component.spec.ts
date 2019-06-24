import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NewProcessComponent } from './new-process.component';

describe('NewProcessComponent', () => {
  let component: NewProcessComponent;
  let fixture: ComponentFixture<NewProcessComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NewProcessComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NewProcessComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
