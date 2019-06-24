import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable} from "rxjs/Observable";
import {of} from "rxjs/observable/of";
import {catchError} from "rxjs/operators";
import {Result} from "../lib/result";
import {TailLog} from "../lib/tail-log";
import {LogConfigurationAndState} from "../lib/log-configuration-and-state";

const skipCache = {headers: new HttpHeaders({'disable-cache':  '1'})};

@Injectable()
export class LogService {

  constructor(
      private http: HttpClient,
      private messageService: MessageService
  ) { }

    public getLog(name: string, offset: number = 0, length: number = 1000): Observable<TailLog> {
        return this.http.get<TailLog>('/api/tail/' + name + '?offset=' + offset + '&length=' + length, skipCache)
            .pipe(
                catchError(this.handleError<TailLog>('get log', null))
            );
    }

    public getErrorLog(name: string, offset: number = 0, length: number = 1000): Observable<TailLog> {
        return this.http.get<TailLog>('/api/error/tail/' + name + '?offset=' + offset + '&length=' + length, skipCache)
            .pipe(
                catchError(this.handleError<TailLog>('get error log', null))
            );
    }

    public clearLog(name: string): Observable<Result> {
        return this.http.post<Result>('/api/log/clear/' + name, null, skipCache)
            .pipe(
                catchError(this.handleError<Result>('clear log', null))
            );
    }

    public createTailLog(config: LogConfigurationAndState): LogConfigurationAndState {
        config.observable = new Observable<string>(observer => {
            let offset = config.offset;
            let loadTime = config.loadTime;
            let name = config.name;
            // console.log("config: " + offset + " " + name);
            const MAX_CHUNK = 500000; // download 0.5 MB maximum per rest call
            const MAX_HISTORY = 6000000; // don't go further back than 6 MB history
            let interval = setInterval(() => {
                // tail doesn't really work in xmlrpc-supervisor, offset is always set to 0 in server (bug), which won't work
                // but tail is handy because it returns a log size which can be used to chuck download the history with normal get log
                this.http.get<TailLog>('/api/tail/' + name + '?offset=0&length=0', skipCache).subscribe(tail => {
                    // console.log("tail: offset " + tail.offset + " overflow "+ tail.overflow);
                    if (tail.offset > MAX_HISTORY && offset == 0) {
                        offset = (tail.offset - MAX_HISTORY); // skip old old history
                        // console.log("skipping history, offset " + offset);
                    }
                    if (offset < tail.offset) {
                        let length = (tail.offset - offset);
                        if (length > MAX_CHUNK) {
                            length = MAX_CHUNK; // limit length to MAX CHUNK
                        }
                        this.http.get<TailLog>('/api/log/' + name + '?offset=' + offset + '&length=' + length, skipCache).subscribe(data => {
                            // console.log("offset " + offset + " tail.offset " + tail.offset);
                            offset += length;
                            data.log = data.log.replace(/\n/g, '<br />');
                            observer.next(data.log);
                            config.update(offset);
                            // console.log("writing config " + offset + " " + name);
                        });
                    } else {
                        // console.log("no new data");
                    }
                }, error => {
                    console.log(error);
                });

            }, loadTime);

            return() => {
                clearInterval(interval);
            }
        });
        return config;
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
