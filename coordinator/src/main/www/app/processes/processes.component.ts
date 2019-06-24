import { Component, OnInit } from '@angular/core';
import {UserService} from "../services/user.service";
import {Router} from "@angular/router";

@Component({
  selector: 'app-processes',
  templateUrl: './processes.component.html',
  styleUrls: ['./processes.component.css'],
})
export class ProcessesComponent implements OnInit {

  constructor(
      private router: Router,
      public userService: UserService
  ) { }

  ngOnInit() {
  }

  addProgram() {
      this.router.navigateByUrl('/add')
  }
}
