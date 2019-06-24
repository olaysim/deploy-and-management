import {ApplicationRef, ChangeDetectorRef, Injectable} from '@angular/core';
import {MatSnackBar} from "@angular/material";
import {NotificationbarComponent} from "../notificationbar/notificationbar.component";
import {FatalErrorMessage} from "../lib/fatal-error-message";
import {ShowSpinner} from "../lib/show-spinner";

@Injectable()
export class MessageService {
    private errorMessage: FatalErrorMessage;
    private spinner = new ShowSpinner();

  constructor(
      private snackBar: MatSnackBar,
      private appRef: ApplicationRef
  ) { }

  public info(msg: string) {
      let data = { type: 'info', message: msg };
      this.snackBar.openFromComponent(NotificationbarComponent, {
          data: data,
          duration: 2500
      });
  }

    public success(msg: string) {
        let data = { type: 'check_circle', message: msg };
        this.snackBar.openFromComponent(NotificationbarComponent, {
            data: data,
            duration: 2500
        });
    }

    public warn(msg: string) {
        let data = { type: 'warning', message: msg };
        this.snackBar.openFromComponent(NotificationbarComponent, {
            data: data,
            duration: 2500
        });
    }

    public error(msg: string) {
        let data = { type: 'error', message: msg };
        this.snackBar.openFromComponent(NotificationbarComponent, {
            data: data,
            duration: 2500
        });
    }

    public setReferenceForFatalErrorMessage(ref: FatalErrorMessage) {
      this.errorMessage = ref;
    }

    public setFatalError(message: string) {
      this.errorMessage.errorMessage = message;
    }

    public clearFatalError() {
      this.errorMessage.errorMessage = null;
    }

    public showSpinner(show: boolean) {
      this.spinner.subject.next(show);
    }

    public getSpinnerSubject() {
      return this.spinner.subject;
    }
}
