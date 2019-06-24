import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavProcessesComponent } from './nav-processes.component';

describe('NavProcessesComponent', () => {
  let component: NavProcessesComponent;
  let fixture: ComponentFixture<NavProcessesComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavProcessesComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavProcessesComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
