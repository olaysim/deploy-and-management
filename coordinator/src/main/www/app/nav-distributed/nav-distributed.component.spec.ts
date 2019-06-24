import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { NavDistributedComponent } from './nav-distributed.component';

describe('NavGroupsComponent', () => {
  let component: NavDistributedComponent;
  let fixture: ComponentFixture<NavDistributedComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ NavDistributedComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(NavDistributedComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
