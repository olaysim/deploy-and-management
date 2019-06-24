import { async, ComponentFixture, TestBed } from '@angular/core/testing';

import { LoginDialogSpinnerComponent } from './login-dialog-spinner.component';

describe('LoginDialogSpinnerComponent', () => {
  let component: LoginDialogSpinnerComponent;
  let fixture: ComponentFixture<LoginDialogSpinnerComponent>;

  beforeEach(async(() => {
    TestBed.configureTestingModule({
      declarations: [ LoginDialogSpinnerComponent ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(LoginDialogSpinnerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
