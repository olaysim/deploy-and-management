import {Component, OnInit} from '@angular/core';
import {UserService} from "../services/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-login-toolbar',
  templateUrl: './login-toolbar.component.html',
  styleUrls: ['./login-toolbar.component.css']
})
export class LoginToolbarComponent implements OnInit {
    screenWidth: number;

    constructor(
      public userService: UserService,
      private router: Router,
    ) {
        this.screenWidth = window.innerWidth;
    }

    ngOnInit() {
    }

    login() {
        if (!this.router.routerState.snapshot.url.startsWith('/login')) {
            this.router.navigate(['/login'], {
                queryParams: {
                    return: this.router.routerState.snapshot.url
                }
            });
        }
    }

    logout() {
      this.userService.logout();
      this.router.navigateByUrl("/");
    }
}

