import {Component, ElementRef, NgZone, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {Program} from "../lib/program";
import {FineUploader} from "fine-uploader";
import {UserService} from "../services/user.service";
import {MessageService} from "../services/message.service";
import {CommandService} from "../services/command.service";
import {FineUploaderProgram} from "../lib/fine-uploader-program";
import { v4 as uuid } from 'uuid';
import {Router} from "@angular/router";

@Component({
  selector: 'app-new-process',
  templateUrl: './new-process.component.html',
  styleUrls: ['./new-process.component.css']
})
export class NewProcessComponent implements OnInit, OnDestroy {
    programModel = new Program();
    uploader: FineUploader;
    @ViewChild('fineUploader') uploaderRef: ElementRef;

  constructor(
      private router: Router,
      public userService: UserService,
      private messageService: MessageService,
      private commandService: CommandService,
      private zone: NgZone
  ) {
      window['angularComponentRef'] = {component: this, zone: zone, componentFn: (succeeded, failed) => this.onAllCompleted(succeeded, failed)}
  }

  ngOnInit() {
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

    ngOnDestroy(): void {
        window['angularComponent'] = null;
    }


    alreadyCompleted: boolean;
    onAllCompleted(succeeded: number[], failed: number[]) {
        if (!this.alreadyCompleted) {
            this.alreadyCompleted = true;
            this.updateAndMove(true);
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

        if (this.hasFiles()) {
            this.messageService.warn("Uploading files");
            this.uploader.uploadStoredFiles();
        } else {
            this.updateAndMove(false);
        }
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

    private updateAndMove(moveData: boolean) {
        this.fuProgram.movedata = moveData;
        this.commandService.moveUploadedData(this.fuProgram).subscribe(res1 => {
            if (res1 != null && res1.success) {
                this.commandService.update().subscribe(res2 => {
                    if (res2 != null && res2.success) {
                        this.messageService.success("Program created!");
                        this.router.navigateByUrl('/processes');
                    }
                });
            } else {
                this.messageService.error("Moving uploaded files into program dir failed!");
            }
        });
    }

    public hasFiles(): boolean {
        return (this.uploader.getUploads() as Array<any>).length > 0;
    }
}
