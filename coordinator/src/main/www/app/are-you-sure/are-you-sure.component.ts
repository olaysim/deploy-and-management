import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material";

@Component({
    selector: 'app-are-you-sure',
    templateUrl: './are-you-sure.component.html',
    styleUrls: ['./are-you-sure.component.css']
})
export class AreYouSureComponent {

    constructor(
        public dialogRef: MatDialogRef<AreYouSureComponent>,
        @Inject(MAT_DIALOG_DATA) public data: any) { }

    onClick(yesno: boolean): void {
        this.dialogRef.close(yesno);
    }
}
