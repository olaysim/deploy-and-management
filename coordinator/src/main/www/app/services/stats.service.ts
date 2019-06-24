import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable} from "rxjs/Observable";
import {of} from "rxjs/observable/of";
import {catchError} from "rxjs/operators";
import {SystemStatistics} from "../lib/system-statistics";
import {SystemInformation} from "../lib/system-information";
import {ProcessStatistics} from "../lib/process-statistics";

@Injectable()
export class StatsService {
    private KIBI = 1024;
    private MEBI = 1048576;
    private GIBI = 1073741824;
    private TEBI = 1099511627776;
    private PEBI = 1125899906842624;
    private EXBI = 1152921504606846976;

  constructor(
      private http: HttpClient,
      private messageService: MessageService
  ) { }

    private systemInformation: SystemInformation;
    public getSystemInformation(): Observable<SystemInformation> {
        return this.http.get<SystemInformation>('/api/stats/info')
            .pipe(
                catchError(this.handleError<SystemInformation>('get system information', null))
            );
    }

    public getSystemStatistics(): Observable<SystemStatistics> {
        return this.http.get<SystemStatistics>('/api/stats')
            .pipe(
                catchError(this.handleError<SystemStatistics>('get system statistics', null))
            );
    }

    public getProcessStatistics(name: string): Observable<ProcessStatistics> {
        return this.http.get<ProcessStatistics>('/api/stats/' + name)
            .pipe(
                catchError(this.handleError<ProcessStatistics>('get process statistics for ' + name, null))
            );
    }


  public formatBytes(bytes: number) {
      if (bytes < this.KIBI) {
          return bytes + ' bytes';
      } else if (bytes < this.MEBI) {
          return (bytes/this.KIBI).toFixed(2) + ' KiB';
      } else if (bytes < this.GIBI) {
          return (bytes/this.MEBI).toFixed(2) + ' MiB';
      } else if (bytes < this.TEBI) {
          return (bytes/this.GIBI).toFixed(2) + ' GiB';
      } else if (bytes < this.PEBI) {
          return (bytes/this.TEBI).toFixed(2) + ' TiB';
      } else if (bytes < this.EXBI) {
          return (bytes/this.TEBI).toFixed(2) + ' PiB';
      } else {
          return (bytes/this.EXBI).toFixed(2) + ' EiB';
      }
  }


    /**
     * Handle Http operation that failed.
     * Let the app continue.
     * @param operation - name of the operation that failed
     * @param result - optional value to return as the observable result
     */
    private handleError<T> (operation: string, result?: T) {
        return (error: any): Observable<T> => {
            if (operation != null && operation != '') {
                console.error('Unable to query server: ' + operation);
            }
            if (error) {
                if (error.error) {
                    if (error.error.message) {
                        // this.messageService.error(error.error.message);
                        console.log(error.error.message);
                    }
                } else {
                    // this.messageService.error(error.statusText);
                    console.log(error.statusText);
                }
            } else {
                // this.messageService.error('Unable to get data');
                console.log('Unable to get data');
            }

            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

}
