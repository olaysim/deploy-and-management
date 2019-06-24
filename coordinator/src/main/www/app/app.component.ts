import {ApplicationRef, ChangeDetectorRef, Component, OnInit} from '@angular/core';
import {ActivatedRoute, NavigationEnd, Router} from "@angular/router";
import {Title} from "@angular/platform-browser";
import 'rxjs/add/operator/filter';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/mergeMap';
import {FatalErrorMessage} from "./lib/fatal-error-message";
import {MessageService} from "./services/message.service";
import {ShowSpinner} from "./lib/show-spinner";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {
    screenWidth: number;
    errorMessage = new FatalErrorMessage();
    showSpinner = false;

    constructor(
        private router: Router,
        private activatedRoute: ActivatedRoute,
        private titleService: Title,
        private messageService: MessageService,
        private cdr: ChangeDetectorRef
    ) {
        this.screenWidth = window.innerWidth;
        window.onresize = () => {
            this.screenWidth = window.innerWidth;
        };
    }

    ngOnInit() {
        this.router.events
            .filter((event) => event instanceof NavigationEnd)
            .map(() => this.activatedRoute)
            .map((route) => {
                while (route.firstChild) route = route.firstChild;
                return route;
            })
            .filter((route) => route.outlet === 'primary')
            .mergeMap((route) => route.data)
            .subscribe((event) => this.titleService.setTitle(this.getTitle(event['title'])));

        this.messageService.setReferenceForFatalErrorMessage(this.errorMessage);
        this.messageService.getSpinnerSubject().subscribe(show => {
            this.showSpinner = show.valueOf();
            this.cdr.detectChanges();
        });
    }

    private getTitle(title: string) {
        return title ? 'Supervisor | ' + title : 'Supervisor';
    }
}
