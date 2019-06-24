import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginToolbarComponent } from './login-toolbar.component';

describe('LoginToolbarComponent', () => {
  let component: LoginToolbarComponent;
  let fixture: ComponentFixture<LoginToolbarComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoginToolbarComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginToolbarComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
