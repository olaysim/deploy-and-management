import {BrowserModule, Title} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from "@angular/forms";
import {HttpClientModule} from "@angular/common/http";
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {FlexLayoutModule} from "@angular/flex-layout";

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {MaterialModule} from "./modules/material.module";
import {ChartsModule} from "ng2-charts";
import {RequestCache, RequestCacheWithMap} from "./services/request-cache.service";

import {MessageService} from "./services/message.service";
import {DashboardComponent} from './dashboard/dashboard.component';
import {NotificationbarComponent} from './notificationbar/notificationbar.component';
import {NotFoundComponent} from './errors/not-found/not-found.component';
import {CookiePolicyComponent} from './cookie-policy/cookie-policy.component';
import {LoginComponent} from './login/login.component';
import {LoginToolbarComponent} from './login-toolbar/login-toolbar.component';
import {UserService} from "./services/user.service";
import {InfoService} from "./services/info.service";
import {NavProcessesComponent} from './nav-processes/nav-processes.component';
import {StatsService} from "./services/stats.service";
import {httpInterceptorProviders} from 'app/http-interceptors';
import {TokenComponent} from './token/token.component';
import {SpinnerComponent} from './spinner/spinner.component';
import {NavDistributedComponent} from './nav-distributed/nav-distributed.component';
import {DistributedComponent} from './distributed/distributed.component';
import {DetailsComponent} from './details/details.component';
import {ProcessesComponent} from './processes/processes.component';
import {GroupsComponent} from './groups/groups.component';
import {ProcessListComponent} from './process-list/process-list.component';
import {CommandService} from "./services/command.service";
import {LogService} from "./services/log.service";
import {NewProcessComponent} from './new-process/new-process.component';
import {LoginDialogSpinnerComponent} from './login-dialog-spinner/login-dialog-spinner.component';
import {TokenService} from "./services/token.service";
import {ClipboardService} from "./services/clipboard.service";
import {AreYouSureComponent} from './are-you-sure/are-you-sure.component';
import { DistributedControlComponent } from './distributed-control/distributed-control.component';
import { DistributedUploadComponent } from './distributed-upload/distributed-upload.component';
import { DistributedInfoComponent } from './distributed-info/distributed-info.component';
import { DistributedLogComponent } from './distributed-log/distributed-log.component';


@NgModule({
    declarations: [
        AppComponent,
        NotificationbarComponent,
        NotFoundComponent,
        CookiePolicyComponent,
        LoginComponent,
        LoginToolbarComponent,
        NavProcessesComponent,
        DashboardComponent,
        TokenComponent,
        SpinnerComponent,
        NavDistributedComponent,
        DistributedComponent,
        DetailsComponent,
        ProcessesComponent,
        GroupsComponent,
        ProcessListComponent,
        NewProcessComponent,
        LoginDialogSpinnerComponent,
        AreYouSureComponent,
        DistributedControlComponent,
        DistributedUploadComponent,
        DistributedInfoComponent,
        DistributedLogComponent
    ],
    imports: [
        BrowserModule,
        FormsModule,
        AppRoutingModule,
        HttpClientModule,
        FlexLayoutModule,
        ChartsModule,
        BrowserAnimationsModule,
        MaterialModule
    ],
    providers: [
        Title,
        MessageService,
        UserService,
        InfoService,
        StatsService,
        CommandService,
        LogService,
        TokenService,
        ClipboardService,
        {provide: RequestCache, useClass: RequestCacheWithMap},
        httpInterceptorProviders
    ],
    entryComponents: [
        NotificationbarComponent,
        LoginDialogSpinnerComponent,
        AreYouSureComponent
    ],
    bootstrap: [AppComponent]
})
export class AppModule { }
