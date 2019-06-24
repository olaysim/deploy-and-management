import { Component, OnInit } from '@angular/core';
import {Router} from "@angular/router";

@Component({
  selector: 'app-nav-distributed',
  templateUrl: './nav-distributed.component.html',
  styleUrls: ['./nav-distributed.component.css']
})
export class NavDistributedComponent implements OnInit {
    chrome: boolean;

  constructor(
      private router: Router,
  ) { }

  ngOnInit() {
      this.chrome = isChrome();
  }

    navigateDistributed(panel: any) {
        // if (panel.panel._expanded) {
            this.router.navigateByUrl("/nodes");
        // }
    }

}

function isChrome() {
    return (window.navigator.userAgent.toLowerCase().indexOf('chrome') > -1);
}
