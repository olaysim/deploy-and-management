import {AfterViewInit, ApplicationRef, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {SupervisorInfo} from "../lib/supervisor-info";
import {InfoService} from "../services/info.service";
import {ProcessInfo} from "../lib/process-info";
import {NodeList} from "../lib/node-list";
import {SystemInformation} from "../lib/system-information";
import {StatsService} from "../services/stats.service";
import {BaseChartDirective} from 'ng2-charts/ng2-charts';
import {Subject} from "rxjs/Subject";
import {ISubscription} from "rxjs/Subscription";
import {MessageService} from "../services/message.service";
import {UserService} from "../services/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, AfterViewInit, OnDestroy {
    supervisorInfo: SupervisorInfo;
    programsInfo: ProcessInfo[];
    nodelist: NodeList;
    systemInformation: SystemInformation;
    @ViewChild('cpuChart') cpuChart: BaseChartDirective;
    @ViewChild('loadChart') loadChart: BaseChartDirective;
    @ViewChild('memChart') memChart: BaseChartDirective;
    @ViewChild('swapChart') swapChart: BaseChartDirective;
    dataReady = new Subject<boolean>();
    sub: ISubscription;

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
            text: 'CPU Load'
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
    userData   = {data: [], label: 'User'};
    niceData   = {data: [], label: 'Nice'};
    systemData = {data: [], label: 'System'};
    idleData   = {data: [], label: 'Idle'};
    cpuSource  = [this.userData, this.systemData, this.niceData, this.idleData];
    labels  = [];
    // LOAD CHART
    loadOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'System Load'
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
    load1Data  = {data: [], label: 'Load 5 min'};
    load2Data  = {data: [], label: 'Load 10 min'};
    load3Data  = {data: [], label: 'Load 15 min'};
    loadSource = [this.load1Data, this.load2Data, this.load3Data];
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
            text: 'Memory'
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
    memData    = {data: [], label: 'Memory usage'};
    // memTotData = {data: [], label: 'Total memory'};
    // memSource  = [this.memData, this.memTotData];
    memSource  = [this.memData];
    // SWAP CHART
    swapOptions = {
        responsive: true,
        elements: {
            point: {
                radius: 0
            }
        },
        title: {
            display: true,
            text: 'Swap'
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
    swapData   = {data: [], label: 'Swap usage'};
    // swapTotData= {data: [], label: 'Total swap'};
    // swapSource = [this.swapData, this.swapTotData];
    swapSource = [this.swapData];

    constructor(
        private router: Router,
        private infoService: InfoService,
        private statsService: StatsService,
        private messageService: MessageService,
        public userService: UserService
    ) { }

    ngOnInit() {
        let loaded = this.supervisorInfo != undefined && this.programsInfo != undefined && this.nodelist != undefined && this.systemInformation != undefined;
        this.messageService.showSpinner(!loaded);
        this.infoService.getSupervisorInfo().subscribe(info => this.supervisorInfo = info);
        this.infoService.getAllProgramsInfo(false).subscribe(info => this.programsInfo = info);
        this.infoService.getNodesList().subscribe(list => this.nodelist = list);
        this.statsService.getSystemInformation().subscribe(info => {
            this.systemInformation = info;

            this.statsService.getSystemStatistics().subscribe(stats => {
                if (stats.weeks.user.length < 5) {
                    for (let i = stats.hour.user.length - 1; i >= 0; i--) {
                        this.labels.push(stats.hour.timestamp - (i * 60000));
                    }
                    for (let val of stats.hour.user) {
                        this.userData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.hour.nice) {
                        this.niceData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.hour.system) {
                        this.systemData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.hour.idle) {
                        this.idleData.data.push(val.toFixed(2));
                    }

                    for (let val of stats.hour.load1) {
                        this.load1Data.data.push(val);
                    }
                    for (let val of stats.hour.load2) {
                        this.load2Data.data.push(val);
                    }
                    for (let val of stats.hour.load3) {
                        this.load3Data.data.push(val);
                    }

                    for (let val of stats.hour.memory) {
                        this.memData.data.push(info.memory - val);
                        // this.memTotData.data.push(info.memory);
                    }

                    for (let val of stats.hour.swap) {
                        this.swapData.data.push(val);
                        // this.swapTotData.data.push(info.swap);
                    }
                } else {
                    for (let i = stats.weeks.user.length - 1; i >= 0; i--) {
                        this.labels.push(stats.weeks.timestamp - (i * 3600000));
                    }
                    for (let val of stats.weeks.user) {
                        this.userData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.weeks.nice) {
                        this.niceData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.weeks.system) {
                        this.systemData.data.push(val.toFixed(2));
                    }
                    for (let val of stats.weeks.idle) {
                        this.idleData.data.push(val.toFixed(2));
                    }

                    for (let val of stats.weeks.load1) {
                        this.load1Data.data.push(val);
                    }
                    for (let val of stats.weeks.load2) {
                        this.load2Data.data.push(val);
                    }
                    for (let val of stats.weeks.load3) {
                        this.load3Data.data.push(val);
                    }

                    for (let val of stats.weeks.memory) {
                        this.memData.data.push(info.memory - val);
                        // this.memTotData.data.push(info.memory);
                    }

                    for (let val of stats.weeks.swap) {
                        this.swapData.data.push(val);
                        // this.swapTotData.data.push(info.swap);
                    }
                }
                this.messageService.showSpinner(false);
                this.dataReady.next(true);
            }, () => {
                this.messageService.showSpinner(false);
            });
        }, () => {
            this.messageService.showSpinner(false);
        });
    }

    ngAfterViewInit(): void {
        this.sub = this.dataReady.subscribe(() => {
            this.cpuChart.chart.update();
            this.loadChart.chart.update();
            this.memChart.chart.update();
            this.swapChart.chart.update();
        });
    }

    ngOnDestroy(): void {
        if (this.sub != null) this.sub.unsubscribe();
        this.messageService.showSpinner(false);
    }

    getRunning() {
        let count = 0;
        for (let item of this.programsInfo) {
            if (item.state == 20) {
                count++;
            }
        }
        return count;
    }

    addProgram() {
        this.router.navigateByUrl('/add')
    }

}
