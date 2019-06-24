import {Injectable} from '@angular/core';
import {catchError} from "rxjs/operators";
import {Observable} from "rxjs/Observable";
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {of} from "rxjs/observable/of";
import {Result} from "../lib/result";
import {SendData} from "../lib/send-data";
import {Program} from "../lib/program";
import {FineUploaderProgram} from "../lib/fine-uploader-program";
import {Group} from "../lib/group";

const skipCache = {headers: new HttpHeaders({'disable-cache':  '1'})};

@Injectable()
export class CommandService {

  constructor(
      private http: HttpClient,
      private messageService: MessageService
  ) { }

    public startProgram(name: string): Observable<Result> {
        return this.http.post<Result>('/api/start/' + name + '?wait=1', null, skipCache)
            .pipe(
                catchError(this.handleError<Result>('start program', null))
            );
    }

    public stopProgram(name: string): Observable<Result> {
        return this.http.post<Result>('/api/stop/' + name + '?wait=1', null, skipCache)
            .pipe(
                catchError(this.handleError<Result>('stop program', null))
            );
    }

    public update(): Observable<Result> {
        return this.http.post<Result>('/api/update', null, skipCache)
            .pipe(
                catchError(this.handleError<Result>('update configuration', null))
            );
    }

    public sendMessage(name: string, message: string): Observable<Result> {
        let sendData = new SendData();
        sendData.data = message;
        return this.http.post<Result>('/api/send/' + name, sendData, skipCache)
            .pipe(
                catchError(this.handleError<Result>('send message', null))
            );
    }

    public sendCommMessage(message: string, type: string): Observable<Result> {
        let sendData = new SendData();
        sendData.data = message;
        sendData.type = type;
        return this.http.post<Result>('/api/sendcomm', sendData, skipCache)
            .pipe(
                catchError(this.handleError<Result>('send comm message', null))
            );
    }

    public sendSignal(name: string, signal: string): Observable<Result> {
        return this.http.post<Result>('/api/signal/' + name + '/' + signal, null, skipCache)
            .pipe(
                catchError(this.handleError<Result>('send signal', null))
            );
    }

    public deleteProrgam(name: string): Observable<Result> {
        if (name == null || name == "") {
            console.error("deleteProrgam was called with no program name");
            return null;
        }
        return this.http.delete<Result>('/api/process/' + name, skipCache)
            .pipe(
                catchError(this.handleError<Result>('delete program', null))
            );
    }

    public getProgramConfiguration(name: string): Observable<Program> {
        if (name == null || name == "") {
            console.error("getProgramConfiguration was called with no program name");
            return null;
        }
        return this.http.get<Program>('/api/configuration/' + name, skipCache)
            .pipe(
                catchError(this.handleError<Program>('get program configuration', new Program()))
            );
    }

    public moveUploadedData(program: FineUploaderProgram): Observable<Result> {
        return this.http.post<Result>('/api/fineuploader/move', program, skipCache)
            .pipe(
                catchError(this.handleError<Result>('move uploaded program data', null))
            );
    }

    public getListOfFiles(program: string): Observable<string[]> {
      if (program == null || program == "") {
          console.error("getListOfFiles was called with no program name");
          return null;
      }
      const url = '/api/process/' + program;
        return this.http.post<string[]>(url, null)
            .pipe(
                catchError(this.handleError<string[]>('get list of files for program', []))
            );
    }

    public getGroupsList(): Observable<Group[]> {
        return this.http.get<Group[]>('/api/group', skipCache)
            .pipe(
                catchError(this.handleError<Group[]>('get group list', []))
            );
    }


    /**
     * Handle Http operation that failed.
     * Let the app continue.
     * @param operation - name of the operation that failed
     * @param result - optional value to return as the observable result
     */
    private handleError<T> (operation: string, result?: T) {
        return (error: any): Observable<T> => {
            console.log(error);
            if (operation != null && operation != '') {
                console.error('Unable to query server: ' + operation);
            }
            if (error) {
                if (error.error) {
                    if (error.error.message) {
                        this.messageService.error(error.error.message);
                    }
                } else {
                    this.messageService.error(error.statusText);
                }
            } else {
                this.messageService.error('Unable to get data');
            }

            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

}
