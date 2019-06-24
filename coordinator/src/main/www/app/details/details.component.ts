import {
    AfterViewInit,
    ChangeDetectorRef,
    Component,
    ElementRef,
    NgZone,
    OnDestroy,
    OnInit,
    ViewChild
} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
import {UserService} from "../services/user.service";
import {MessageService} from "../services/message.service";
import {ProcessInfo} from "../lib/process-info";
import {InfoService} from "../services/info.service";
import {LogService} from "../services/log.service";
import {ISubscription} from "rxjs/Subscription";
import {LogConfigurationAndState} from "../lib/log-configuration-and-state";
import {BaseChartDirective} from "ng2-charts/ng2-charts";
import {Subject} from "rxjs/Subject";
import {StatsService} from "../services/stats.service";
import {MessageModel} from "../lib/message-model";
import {CommandService} from "../services/command.service";
import {MatDialog} from "@angular/material";
import {AreYouSureComponent} from "../are-you-sure/are-you-sure.component";
import {Program} from "../lib/program";
import {FineUploader} from 'fine-uploader';
import { v4 as uuid } from 'uuid';
import {FineUploaderProgram} from "../lib/fine-uploader-program";

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.css']
})
export class DetailsComponent implements OnInit, OnDestroy, AfterViewInit {
    public name: string;
    public selectedIndex = 0;
    public processInfo: ProcessInfo = new ProcessInfo();
    public log = '';
    private sub: ISubscription;
    public trucking = false;
    private logState: LogConfigurationAndState;
    private dataReady = new Subject<boolean>();
    private subChart: ISubscription;
    public programFiles: string[];

    messageModel = new MessageModel(true);
    messageOptions = [];
    commMessageModel = new MessageModel();
    commMessageOptions = [];
    commMessageTypeOptions = [];
    signalModel = new MessageModel();
    signalOptions = ['SIGHUP', 'SIGINT', 'SIGQUIT', 'SIGILL', 'SIGTRAP', 'SIGABRT', 'SIGBUS', 'SIGFPE', 'SIGKILL', 'SIGUSR1', 'SIGSEGV', 'SIGUSR2', 'SIGPIPE', 'SIGALRM', 'SIGTERM', 'SIGSTKFLT', 'SIGCHLD', 'SIGCONT', 'SIGSTOP', 'SIGTSTP', 'SIGTTIN', 'SIGTTOU', 'SIGURG', 'SIGXCPU', 'SIGXFSZ', 'SIGVTALRM', 'SIGPROF', 'SIGWINCH', 'SIGIO', 'SIGPWR', 'SIGSYS', 'SIGRTMIN', 'SIGRTMIN+1', 'SIGRTMIN+2', 'SIGRTMIN+3', 'SIGRTMIN+4', 'SIGRTMIN+5', 'SIGRTMIN+6', 'SIGRTMIN+7', 'SIGRTMIN+8', 'SIGRTMIN+9', 'SIGRTMIN+10', 'SIGRTMIN+11', 'SIGRTMIN+12', 'SIGRTMIN+13', 'SIGRTMIN+14', 'SIGRTMIN+15', 'SIGRTMAX-14', 'SIGRTMAX-13', 'SIGRTMAX-12', 'SIGRTMAX-11', 'SIGRTMAX-10', 'SIGRTMAX-9', 'SIGRTMAX-8', 'SIGRTMAX-7', 'SIGRTMAX-6', 'SIGRTMAX-5', 'SIGRTMAX-4', 'SIGRTMAX-3', 'SIGRTMAX-2', 'SIGRTMAX-1', 'SIGRTMAX'];

    @ViewChild('cpuChart') cpuChart: BaseChartDirective;
    @ViewChild('memChart') memChart: BaseChartDirective;
    @ViewChild('vszChart') vszChart: BaseChartDirective;
    @ViewChild('rssChart') rssChart: BaseChartDirective;

    programModel = new Program();
    uploader: FineUploader;
    @ViewChild('fineUploader') uploaderRef: ElementRef;
    @ViewChild('programForm') programFormRef: ElementRef;

    // CPU CHART
    cpuOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'CPU Load Percentage'
        },
        legend: {
            position: 'bottom'
        },
        scales: {
            xAxes: [{
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 10,
                    callback: function(value) {
                        let days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                        let d = new Date(value);
                        let day = days[d.getDay()];
                        let hour = d.getHours();
                        return day + ':' + hour;
                    }
                }
            }]
        }
    };
    cpuData   = {data: [], label: 'CPU'};
    cpuSource = [this.cpuData];
    labels    = [];
    //cpuColors = [{backgroundColor: "rgba(220,220,220,0.2)", borderColor: "rgba(220,220,220,1)"}]; // light gray
    //cpuColors = [{backgroundColor: "rgba(151,187,205,0.2)", borderColor: "rgba(151,187,205,1)"}]; // light blue
    //cpuColors = [{backgroundColor: "rgba(247,70,74,0.2)", borderColor: "rgba(247,70,74,1)"}]; // red
    //cpuColors = [{backgroundColor: "rgba(70,191,189,0.2)", borderColor: "rgba(70,191,189,1"}]; // green
    //cpuColors = [{backgroundColor: "rgba(253,180,92,0.2)", borderColor: "rgba(253,180,92,1)"}]; // yellow
    // MEMORY CHART
    memOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'Memory Usage Percentage'
        },
        legend: {
            position: 'bottom'
        },
        scales: {
            xAxes: [{
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 10,
                    callback: function(value) {
                        let days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                        let d = new Date(value);
                        let day = days[d.getDay()];
                        let hour = d.getHours();
                        return day + ':' + hour;
                    }
                }
            }]
        }
    };
    memData  = {data: [], label: 'Memory'};
    memSource = [this.memData];
    memColors = [{backgroundColor: "rgba(0,148,255,0.2)", borderColor: "rgba(0,148,255,1)"}]; // light blue
    // VSZ CHART
    vszOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'Virtual Memory Size'
        },
        legend: {
            position: 'bottom'
        },
        scales: {
            xAxes: [{
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 10,
                    callback: function(value) {
                        let days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                        let d = new Date(value);
                        let day = days[d.getDay()];
                        let hour = d.getHours();
                        return day + ':' + hour;
                    }
                }
            }],
            yAxes: [{
                ticks: {
                    callback: function(value) {
                        let MEBI = 1048576;
                        let GIBI = 1073741824;
                        if (value < GIBI) {
                            return (value/MEBI).toFixed(1) + ' MiB';
                        } else {
                            return (value/GIBI).toFixed(1) + ' GiB';
                        }
                    }
                }
            }]
        }
    };
    vszData    = {data: [], label: 'VSZ'};
    vszSource  = [this.vszData];
    vszColors = [{backgroundColor: "rgba(0,175,0,0.2)", borderColor: "rgba(0,175,0,1"}]; // green
    // RSS CHART
    rssOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'Resident Set Size'
        },
        legend: {
            position: 'bottom'
        },
        scales: {
            xAxes: [{
                ticks: {
                    autoSkip: true,
                    maxTicksLimit: 10,
                    callback: function(value) {
                        let days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
                        let d = new Date(value);
                        let day = days[d.getDay()];
                        let hour = d.getHours();
                        return day + ':' + hour;
                    }
                }
            }],
            yAxes: [{
                ticks: {
                    callback: function(value) {
                        let MEBI = 1048576;
                        let GIBI = 1073741824;
                        if (value < GIBI) {
                            return (value/MEBI).toFixed(1) + ' MiB';
                        } else {
                            return (value/GIBI).toFixed(1) + ' GiB';
                        }
                    }
                }
            }]
        }
    };
    rssData   = {data: [], label: 'RSS'};
    rssSource = [this.rssData];
    rssColors = [{backgroundColor: "rgba(253,180,92,0.2)", borderColor: "rgba(253,180,92,1)"}]; // yellow

  constructor(
      private activatedRoute: ActivatedRoute,
      private router: Router,
      public userService: UserService,
      private messageService: MessageService,
      private infoService: InfoService,
      private logService: LogService,
      private cdr: ChangeDetectorRef,
      private statsService: StatsService,
      private commandService: CommandService,
      private dialog: MatDialog,
      private zone: NgZone
  ) {
      this.router.routeReuseStrategy.shouldReuseRoute = function() {return false;};
      window['angularComponentRef'] = {component: this, zone: zone, componentFn: (succeeded, failed) => this.onAllCompleted(succeeded, failed)}
  }

  ngOnInit() {
      this.name = this.activatedRoute.snapshot.paramMap.get('name');
      const tab = this.activatedRoute.snapshot.queryParams['tab'];
      this.logState = new LogConfigurationAndState(this.name);
      if (tab == 'log') {
          this.selectedIndex = 1;
          this.trucking = true;
          this.logState = this.logService.createTailLog(this.logState);
          this.sub = this.logState.observable.subscribe(data => {
              this.log += data;
              this.cdr.detectChanges();
          });
      }
      this.infoService.getProgramInfo(this.name).subscribe(info => this.processInfo = info);

      this.statsService.getProcessStatistics(this.name).subscribe(stats => {
          if (stats != null) {
              if (stats.weeks.cpu.length < 5) {
                  for (let i = stats.hour.cpu.length - 1; i >= 0; i--) {
                      this.labels.push(stats.hour.timestamp - (i * 60000));
                  }
                  for (let val of stats.hour.cpu) {
                      this.cpuData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.hour.memory) {
                      this.memData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.hour.vsz) {
                      this.vszData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.hour.rss) {
                      this.rssData.data.push(val.toFixed(2));
                  }
              } else {
                  for (let i = stats.weeks.cpu.length - 1; i >= 0; i--) {
                      this.labels.push(stats.weeks.timestamp - (i * 3600000));
                  }
                  for (let val of stats.weeks.cpu) {
                      this.cpuData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.weeks.memory) {
                      this.memData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.weeks.vsz) {
                      this.vszData.data.push(val.toFixed(2));
                  }
                  for (let val of stats.weeks.rss) {
                      this.rssData.data.push(val.toFixed(2));
                  }
              }
              this.dataReady.next(true);
          }
      });

      this.messageOptions = this.loadStringArray('messageoptions');
      this.commMessageOptions = this.loadStringArray('commmessageoptions');
      this.commMessageTypeOptions = this.loadStringArray('commmessagetypeoptions');

      this.commandService.getProgramConfiguration(this.name).subscribe(conf => {
          if (conf != null) {
              this.programModel = conf;
              if (conf.autostart == null) this.programModel.autostart = true;
          }
      });

      if (this.userService.isLoggedIn()) {
          this.commandService.getListOfFiles(this.name).subscribe(list => this.programFiles = list);
      }

      this.uploader = new FineUploader({
          debug: false,
          autoUpload: false,
          request: {
              endpoint: '/api/fineuploader/upload',
              customHeaders: {
                  'Authorization': 'Bearer ' + this.userService.getToken()
              }
          },
          dragAndDrop: {
              reportDirectoryPaths: true
          },
          chunking: {
              enabled: true,
              concurrent: {
                  enabled: true
              },
              success: {
                  endpoint: "/api/fineuploader/done"
              },
              partSize: 1000000
          },
          element: this.uploaderRef.nativeElement,
          callbacks: {
              onAllComplete: (succeeded, failed) => {
                  window['angularComponentRef'].zone.run(() => window['angularComponentRef'].componentFn(succeeded, failed));
              }
          }
      });
  }



    ngAfterViewInit(): void {
        this.subChart = this.dataReady.subscribe(() => {
            try {this.cpuChart.chart.update();} catch (ignore) {}
            try {this.memChart.chart.update();} catch (ignore) {}
            try {this.vszChart.chart.update();} catch (ignore) {}
            try {this.rssChart.chart.update();} catch (ignore) {}
        });
    }

    public messageFilteredOptions: string[];
    public commMessageFilteredOptions: string[];
    public commMessageTypeFilteredOptions: string[];
    public signalFilteredOptions: string[];
    filterOptions(val: string, options: string[]): string[] {
      return options.filter(option => option.toLowerCase().indexOf(val.toLowerCase()) > -1);
    }

    onTabClick(event: any) {
      if (event.index == 1) {
          this.trucking = true;
          this.logState = this.logService.createTailLog(this.logState);
          this.sub = this.logState.observable.subscribe(data => {
              this.log += data;
              this.cdr.detectChanges();
          });
      } else {
          this.trucking = false;
          if (this.sub != null) this.sub.unsubscribe();
          this.logState.observable = null;
      }
        this.cdr.detectChanges();
    }

  clearLog() {
      this.logService.clearLog(this.name).subscribe(result => {
          if (result.success) {
              this.messageService.info("Program log cleared!");
              this.log = '';
              this.cdr.detectChanges();
          } else {
              this.messageService.error("Unable to clear log");
          }
      });
  }

    ngOnDestroy(): void {
        if (this.sub != null) this.sub.unsubscribe();
        if (this.subChart != null) this.subChart.unsubscribe();
        if (this.logState != null) this.logState.observable = null;
        this.trucking = false;
        window['angularComponent'] = null;
    }

    toggleLog() {
        if (this.trucking) {
            if (this.sub != null) this.sub.unsubscribe();
            this.logState.observable = null;
            this.trucking = false;
        } else {
            this.trucking = true;
            this.logState = this.logService.createTailLog(this.logState);
            this.sub = this.logState.observable.subscribe(data => {
                this.log += data;
                this.cdr.detectChanges();
            });
        }
    }

    private appendNewline(message: string, append: boolean): string {
      if (append && !message.endsWith('\n')) {
          return message + '\n';
      } else if (!append && message.endsWith('\n')) {
          return message.substr(0, message.lastIndexOf('\n'));
      } else {
          return message;
      }
    }

    onMessageSubmit() {
        let add = true;
        for (let str of this.messageOptions) {
            if (str == this.messageModel.message) add = false;
        }
        if (add) {
            this.messageOptions.push(this.messageModel.message);
            this.saveStringArray('messageoptions', this.messageOptions);
        }
        this.messageModel.message = this.appendNewline(this.messageModel.message, this.messageModel.appendNewline);
        this.commandService.sendMessage(this.name, this.messageModel.message).subscribe(result => {
            if (result != null && result.success) {
                this.messageService.info("Message sent to process");
            }
        });
    }

    onCommSubmit() {
        let add = true;
        for (let str of this.commMessageOptions) {
            if (str == this.commMessageModel.message) add = false;
        }
        if (add) {
            this.commMessageOptions.push(this.commMessageModel.message);
            this.saveStringArray('commmessageoptions', this.commMessageOptions);
        }
        add = true;
        for (let str of this.commMessageTypeOptions) {
            if (str == this.commMessageModel.messageType) add = false;
        }
        if (add) {
            this.commMessageTypeOptions.push(this.commMessageModel.messageType);
            this.saveStringArray('commmessagetypeoptions', this.commMessageTypeOptions);
        }
        this.commMessageModel.message = this.appendNewline(this.commMessageModel.message, this.commMessageModel.appendNewline);
        this.commandService.sendCommMessage(this.commMessageModel.message, this.commMessageModel.messageType).subscribe(result => {
            if (result != null && result.success) {
                this.messageService.info("Comm Message sent to server");
            }
        });
    }

    onSignalSubmit() {
        this.commandService.sendSignal(this.name, this.signalModel.message).subscribe(result => {
            if (result != null && result.success) {
                this.messageService.info("Signal sent to process");
            }
        });
    }

    private loadStringArray(key: string): string[] {
      let result = [];
      try {
          result = JSON.parse(localStorage.getItem(key));
      } catch (ignore) {}
      return result == null ? [] : result;
    }

    private saveStringArray(key: string, array: string[]) {
        try {
            if (array.length > 3) {
                let idx = array.length - 3;
                localStorage.setItem(key, JSON.stringify(array.slice(idx)));
            } else {
                localStorage.setItem(key, JSON.stringify(array));
            }
        } catch (ignore) {}
    }

    clearMessageHistory() {
      localStorage.removeItem('messageoptions');
      this.messageOptions = [];
      if (this.messageModel != null) {
          this.messageModel.message = '';
      }
    }

    clearCommMessageHistory() {
        localStorage.removeItem('commmessageoptions');
        this.commMessageOptions = [];
        if (this.commMessageModel != null) {
            this.commMessageModel.message = '';
        }
    }

    clearCommMessageTypeHistory() {
        localStorage.removeItem('commmessagetypeoptions');
        this.commMessageTypeOptions = [];
        if (this.commMessageModel != null) {
            this.commMessageModel.messageType = '';
        }
    }

    deleteProgram() {
      let ref = this.dialog.open(AreYouSureComponent, {
          data: {message: 'Do you want to delete ' + this.name + '?'}
      });
      ref.afterClosed().subscribe(result => {
          if (result != null && result == true) {
              this.messageService.warn("Stopping program");
              this.commandService.stopProgram(this.name).subscribe(result => {
                  if (result.success || result.description.toUpperCase().indexOf('NOT_RUNNING') > -1) {
                      this.messageService.warn("Removing program");
                      this.commandService.deleteProrgam(this.name).subscribe(delRes => {
                          if (delRes != null && delRes.success) {
                              this.messageService.success(this.name + ' deleted!');
                              this.commandService.update().subscribe(upRes => {
                                  if (upRes != null && upRes.success) {
                                      this.router.navigateByUrl('/');
                                  } else {
                                      this.messageService.info("Program deleted, but supervisor was not updated");
                                  }
                              });
                          }
                      });
                  } else {
                      this.messageService.error("Unable to stop program, cannot remove");
                  }
              });
          }
      })
    }

    alreadyCompleted: boolean;
    onAllCompleted(succeeded: number[], failed: number[]) {
        if (!this.alreadyCompleted) {
            this.alreadyCompleted = true;
            this.updateAndRestart(true);
            this.uploader.reset();
        }
    }

    transaction: string;
    fuProgram: FineUploaderProgram;
    onProgramSubmit() {
        this.alreadyCompleted = false;
        let transid = uuid();
        this.transaction = transid;
        this.uploader.setParams({
          'qqtransaction': transid
        });
        this.fuProgram = new FineUploaderProgram();
        this.fuProgram.transaction = transid;
        this.prepareSave();
        for (let item of this.uploader.getUploads() as Array<any>) {
            this.fuProgram.uuidFilenames[item.uuid] = item.name;
            if (item.file['qqPath'] != null) {
                this.fuProgram.uuidPaths[item.uuid] = item.file['qqPath'];
            }
        }

        this.messageService.warn("Stopping program");
        this.commandService.stopProgram(this.name).subscribe(res1 => {
            if (res1 != null && (res1.success || res1.description.toUpperCase().indexOf('NOT_RUNNING') > -1)) {
                if ((this.uploader.getUploads() as Array<any>).length > 0) {
                    this.messageService.warn("Uploading files");
                    this.uploader.uploadStoredFiles();
                } else {
                    this.updateAndRestart(false);
                }
            }
        });
    }

    private prepareSave() {
        this.fuProgram.name = this.programModel.name;
        this.fuProgram.command = this.programModel.command;
        if (this.programModel.priority != null) this.fuProgram.priority = this.programModel.priority;
        if (this.programModel.autostart != null) this.fuProgram.autostart = this.programModel.autostart;
        if (this.programModel.autorestart != null) this.fuProgram.autorestart = this.programModel.autorestart;
        if (this.programModel.startsecs != null) this.fuProgram.startsecs = this.programModel.startsecs;
        if (this.programModel.startretries != null) this.fuProgram.startretries = this.programModel.startretries;
        if (this.programModel.exitcodes != null) this.fuProgram.exitcodes = this.programModel.exitcodes;
        if (this.programModel.stopwaitsecs != null) this.fuProgram.stopwaitsecs = this.programModel.stopwaitsecs;
        if (this.programModel.environment != null) this.fuProgram.environment = this.programModel.environment;
    }
    private updateAndRestart(moveData: boolean) {
        this.fuProgram.movedata = moveData;
        this.commandService.moveUploadedData(this.fuProgram).subscribe(res1 => {
            if (res1 != null && res1.success) {
                this.commandService.update().subscribe(res2 => {
                    if (res2 != null && res2.success) {
                        if (this.processInfo.state == 20) {
                            this.messageService.warn("Starting program");
                            this.commandService.startProgram(this.name).subscribe(res3 => {
                                if (res3 != null && (res3.success || res3.description.toUpperCase().indexOf('ALREADY_STARTED'))) {
                                    this.messageService.success("Program updated!");
                                }
                            });
                        } else {
                            this.messageService.success("Program updated!");
                        }
                    }
                });
            } else {
                this.messageService.error("Moving uploaded files into program dir failed!");
            }
        });

    }


    start() {
        this.processInfo.state = 10;
        this.processInfo.statename = 'STARTING';
        this.messageService.warn("Starting program");
        this.commandService.startProgram(this.name).subscribe(result => {
            if (result.success) {
                this.infoService.getProgramInfo(this.name).subscribe(info => this.processInfo = info);
                this.messageService.success("Program started!");
            } else {
                this.messageService.error("Unable to start program: " + this.name);
            }
        });
    }

    stop() {
        this.processInfo.state = 40;
        this.processInfo.statename = 'STOPPING';
        this.messageService.warn("Stopping program");
        this.commandService.stopProgram(this.name).subscribe(result => {
            if (result.success) {
                this.infoService.getProgramInfo(this.name).subscribe(info => this.processInfo = info);
                this.messageService.success("Program stopped!");
            } else {
                this.messageService.error("Unable to stop program: " + this.name);
            }
        });
    }

    restart() {
        this.processInfo.state = 40;
        this.processInfo.statename = 'STOPPING';
        this.messageService.warn("Stopping program");
        this.commandService.stopProgram(this.name).subscribe(result => {
            if (result.success || result.description.toUpperCase().indexOf('NOT_RUNNING') > -1) {
                this.commandService.update().subscribe(result => {
                    if (result.success) {
                        this.processInfo.state = 10;
                        this.processInfo.statename = 'STARTING';
                        this.messageService.warn("Starting program");
                        this.commandService.startProgram(this.name).subscribe(result => {
                            if (result.success) {
                                this.infoService.getProgramInfo(this.name).subscribe(info => this.processInfo = info);
                                this.messageService.success("Program started!");
                            } else {
                                this.messageService.error("Unable to start program: " + this.name);
                            }
                        });
                    } else {
                        this.processInfo.state = 1000;
                        this.processInfo.statename = 'UNKNOWN';
                        this.messageService.error("Unable to update program configuration");
                    }
                });
            } else {
                this.processInfo.state = 1000;
                this.processInfo.statename = 'UNKNOWN';
                this.messageService.error("Unable to stop program: " + this.name);
            }
        });
    }
}
