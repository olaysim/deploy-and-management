import {NgModule} from '@angular/core';
import {RouterModule, Routes} from "@angular/router";
import {NotFoundComponent} from "./errors/not-found/not-found.component";
import {CookiePolicyComponent} from "./cookie-policy/cookie-policy.component";
import {LoginComponent} from "./login/login.component";
import {DashboardComponent} from "./dashboard/dashboard.component";
import {TokenComponent} from "./token/token.component";
import {DistributedComponent} from "./distributed/distributed.component";
import {DetailsComponent} from "./details/details.component";
import {ProcessesComponent} from "./processes/processes.component";
import {GroupsComponent} from "./groups/groups.component";
import {NewProcessComponent} from "./new-process/new-process.component";
import {DistributedControlComponent} from "./distributed-control/distributed-control.component";
import {DistributedUploadComponent} from "./distributed-upload/distributed-upload.component";
import {DistributedInfoComponent} from "./distributed-info/distributed-info.component";
import {DistributedLogComponent} from "./distributed-log/distributed-log.component";

const routes: Routes = [
    {path: '', component: DashboardComponent},
    {path: 'processes', component: ProcessesComponent, data: {title: 'Processes'}},
    {path: 'details/:name', component: DetailsComponent, data: {title: 'Process Details'}},
    {path: 'login', component: LoginComponent, data: {title: 'Login'}},
    {path: 'token', component: TokenComponent, data: {title: 'Generate Token'}},
    {path: 'nodes', component: DistributedComponent, data: {title: 'Distributed Nodes'}},
    {path: 'nodes/control', component: DistributedControlComponent, data: {title: 'Distributed Control'}},
    {path: 'nodes/upload', component: DistributedUploadComponent, data: {title: 'Distributed Upload'}},
    {path: 'nodes/info', component: DistributedInfoComponent, data: {title: 'Distributed Info'}},
    {path: 'nodes/log', component: DistributedLogComponent, data: {title: 'Distributed Log'}},
    {path: 'groups', component: GroupsComponent, data: {title: 'Groups'}},
    {path: 'add', component: NewProcessComponent, data: {title: 'New Program'}},
    {path: 'policy', component: CookiePolicyComponent, data: {title: 'Cookie Policy'}},
    {path: '**', component: NotFoundComponent, data: {title: '404'}}
];

@NgModule({
    imports: [ RouterModule.forRoot(routes)],
    exports: [ RouterModule ]
})
export class AppRoutingModule { }
