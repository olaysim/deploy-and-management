import {Component, Input, OnInit} from '@angular/core';
import {StatsService} from "../services/stats.service";
import {MessageService} from "../services/message.service";
import {InfoService} from "../services/info.service";
import {ProcessInfo} from "../lib/process-info";
import {Router} from "@angular/router";
import {UserService} from "../services/user.service";
import {CommandService} from "../services/command.service";

@Component({
  selector: 'app-process-list',
  templateUrl: './process-list.component.html',
  styleUrls: ['./process-list.component.css']
})
export class ProcessListComponent implements OnInit {
    @Input() onDashboard = false;
    programsInfo: ProcessInfo[];
    displayedColumns: string[];

  constructor(
      private router: Router,
      private infoService: InfoService,
      private statsService: StatsService,
      private messageService: MessageService,
      public userService: UserService,
      private commandService: CommandService
  ) { }

  ngOnInit() {
      // this.displayedColumns = this.onDashboard ?  ['name', 'pid', 'state'] :  ['name', 'group', 'status', 'pid', 'state', 'control'];
      this.displayedColumns = ['name', 'group', 'status', 'pid', 'state', 'control'];
      this.infoService.getAllProgramsInfo(false).subscribe(info => this.programsInfo = info);
  }

    start(name: string) {
      for(let pi of this.programsInfo) {
          if (pi.name == name) {
              pi.state = 10;
              pi.statename = 'STARTING';
              break;
          }
      }
        this.commandService.startProgram(name).subscribe(result => {
            if (result.success) {
                this.infoService.getAllProgramsInfo(false).subscribe(info => this.programsInfo = info);
            } else {
                this.messageService.error("Unable to start program: " + name);
            }
        });
    }

    stop(name: string) {
        for(let pi of this.programsInfo) {
            if (pi.name == name) {
                pi.state = 40;
                pi.statename = 'STOPPING';
                break;
            }
        }
        this.commandService.stopProgram(name).subscribe(result => {
            if (result.success) {
                this.infoService.getAllProgramsInfo(false).subscribe(info => this.programsInfo = info);
            } else {
                this.messageService.error("Unable to stop program: " + name);
            }
        });
    }

    restart(name: string) {
        for(let pi of this.programsInfo) {
            if (pi.name == name) {
                pi.state = 40;
                pi.statename = 'STOPPING';
                break;
            }
        }
        this.commandService.stopProgram(name).subscribe(result => {
            if (result.success || result.description.toUpperCase().indexOf('NOT_RUNNING') > -1) {
                this.commandService.update().subscribe(result => {
                    if (result.success) {
                        for(let pi of this.programsInfo) {
                            if (pi.name == name) {
                                pi.state = 10;
                                pi.statename = 'STARTING';
                                break;
                            }
                        }
                        this.commandService.startProgram(name).subscribe(result => {
                            if (result.success) {
                                this.infoService.getAllProgramsInfo(false).subscribe(info => this.programsInfo = info);
                            } else {
                                this.messageService.error("Unable to start program: " + name);
                            }
                        });
                    } else {
                        for(let pi of this.programsInfo) {
                            if (pi.name == name) {
                                pi.state = 1000;
                                pi.statename = 'UNKNOWN';
                                break;
                            }
                        }
                        this.messageService.error("Unable to update program configuration");
                    }
                });
            } else {
                for(let pi of this.programsInfo) {
                    if (pi.name == name) {
                        pi.state = 1000;
                        pi.statename = 'UNKNOWN';
                        break;
                    }
                }
                this.messageService.error("Unable to stop program: " + name);
            }
        });
    }

    log(name: string) {
        this.router.navigate(['/details/' + name], {
            queryParams: {
                tab: 'log'
            }
        });
    }

    selectRow(row: any) {
        this.router.navigateByUrl('/details/' + row.name);
    }
}
