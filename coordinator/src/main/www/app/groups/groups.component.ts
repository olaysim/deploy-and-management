import {Component, OnDestroy, OnInit} from '@angular/core';
import {MessageService} from "../services/message.service";
import {CommandService} from "../services/command.service";
import {Router} from "@angular/router";
import {UserService} from "../services/user.service";
import {Group} from "../lib/group";
import {ProgramGroup} from "../lib/program-group";

@Component({
  selector: 'app-groups',
  templateUrl: './groups.component.html',
  styleUrls: ['./groups.component.css']
})
export class GroupsComponent implements OnInit, OnDestroy {
    public groupList: Group[];
    public groupOptions = [];
    public groupFilteredOptions: string[];
    public programGroup = new ProgramGroup();


  constructor(
      private router: Router,
      public userService: UserService,
      private messageService: MessageService,
      private commandService: CommandService,
  ) { }

  ngOnInit() {
      this.commandService.getGroupsList().subscribe(list => {
          this.groupList = list;
          for (let group of list) {
              this.groupOptions.push(group.name);
          }
      });
  }

    ngOnDestroy(): void {
    }

    filterOptions(val: string, options: string[]): string[] {
        return options.filter(option => option.toLowerCase().indexOf(val.toLowerCase()) > -1);
    }

    onAddProgramSubmit() {

    }
}
