import {Component, OnInit} from '@angular/core';
import {ProcessInfo} from "../lib/process-info";
import {InfoService} from "../services/info.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-nav-processes',
  templateUrl: './nav-processes.component.html',
  styleUrls: ['./nav-processes.component.css']
})
export class NavProcessesComponent implements OnInit {
    programsInfo: ProcessInfo[];
    chrome: boolean;

  constructor(
      private router: Router,
      private infoService: InfoService
  ) { }

  ngOnInit() {
      this.chrome = isChrome();
      this.infoService.getAllProgramsInfo().subscribe(info => this.programsInfo = info);
  }

    navigateProcesses(panel: any) {
      // if (panel.panel._expanded) {
          this.router.navigateByUrl("/processes");
      // }
    }
}

function isChrome() {
    return (window.navigator.userAgent.toLowerCase().indexOf('chrome') > -1);
}
