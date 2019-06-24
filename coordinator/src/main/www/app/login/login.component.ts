import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    OnDestroy,
    OnInit,
    ViewChild,
    ViewChildren
} from '@angular/core';
import {Login} from "../lib/login";
import {UserService} from "../services/user.service";
import {MessageService} from "../services/message.service";
import {ActivatedRoute, Router} from "@angular/router";
import {ISubscription} from "rxjs/Subscription";
import {LoginDialogSpinnerComponent} from "../login-dialog-spinner/login-dialog-spinner.component";
import {MatDialog} from "@angular/material";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit, OnDestroy, AfterViewInit {
    screenWidth: number;
    model = new Login();
    returnUrl: string;
    loginWasSuccessful = true;
    sub: ISubscription;
    loginDialogRef: any;
    @ViewChild('input') inputbox: ElementRef;

  constructor(
      public userService: UserService,
      private router: Router,
      private route: ActivatedRoute,
      private dialog: MatDialog,
      private cdr: ChangeDetectorRef
  ) {
      this.screenWidth = window.innerWidth;
  }

  ngOnInit() {
      this.returnUrl = this.route.snapshot.queryParams['return'] || '/';
  }

  onSubmit() {
      this.loginDialogRef = this.dialog.open(LoginDialogSpinnerComponent, {
          width: '250'
      });
      this.sub = this.userService.login(this.model)
          .subscribe(success => {
              if (success) {
                  this.router.navigateByUrl(this.returnUrl);
              } else {
                  this.loginWasSuccessful = false;
              }
          });
      if (this.loginDialogRef != null) this.loginDialogRef.close();
  }

    logout() {
        this.userService.logout();
        this.router.navigateByUrl("/login");
    }

    ngOnDestroy(): void {
      if (this.sub != null) this.sub.unsubscribe();
      if (this.loginDialogRef != null) this.loginDialogRef.close();
    }

    ngAfterViewInit(): void {
      this.inputbox.nativeElement.focus();
      this.cdr.detectChanges();
    }
}
