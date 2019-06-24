import {ChangeDetectorRef, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {MessageService} from "../services/message.service";
import {UserService} from "../services/user.service";
import * as JWT from 'jwt-decode';
import {TokenModel} from "../lib/token-model";
import {TokenService} from "../services/token.service";
import {ClipboardService} from "../services/clipboard.service";

@Component({
  selector: 'app-token',
  templateUrl: './token.component.html',
  styleUrls: ['./token.component.css']
})
export class TokenComponent implements OnInit {
    tokenModel = new TokenModel();
    decodedToken: any;
    isAdmin: boolean = false;
    generatedToken: string;

  constructor(
      public userService: UserService,
      private messageService: MessageService,
      private tokenService: TokenService,
      private clipboardService: ClipboardService
  ) { }

  ngOnInit() {
      try {
          if (this.userService.isLoggedIn()) {
              this.decodedToken = JWT(this.userService.getToken());
          }
      } catch (ignore) {
          this.decodedToken = null;
      }
      try {
          if (this.decodedToken != null) {
              this.isAdmin = this.decodedToken.admin;
          }
      } catch (ignore) {}
  }

    onGenerateSubmit() {
        this.tokenService.generateToken(this.tokenModel).subscribe(token => {
            if (token != null) {
                this.generatedToken = token.token;
            }
        });
    }

    copyTokenToClipboard() {
        this.clipboardService.copy(this.generatedToken);
        this.messageService.info("Token copied to clipboard")
    }
}
