import {Component, Inject} from '@angular/core';
import {MAT_SNACK_BAR_DATA} from "@angular/material";

@Component({
  selector: 'app-notificationbar',
  templateUrl: './notificationbar.component.html',
  styleUrls: ['./notificationbar.component.css']
})
export class NotificationbarComponent {

  constructor(@Inject(MAT_SNACK_BAR_DATA) public data: any) { }

}
