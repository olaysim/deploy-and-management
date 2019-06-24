import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Observable";
import {catchError} from "rxjs/operators";
import {SupervisorInfo} from "../lib/supervisor-info";
import {ProcessInfo} from "../lib/process-info";
import {NodeList} from "../lib/node-list";

const skipCache = {headers: new HttpHeaders({'disable-cache':  '1'})};

@Injectable()
export class InfoService {

  constructor(
      private http: HttpClient,
      private messageService: MessageService
  ) { }

    public getSupervisorInfo(): Observable<SupervisorInfo> {
        return this.http.get<SupervisorInfo>('/api/info')
            .pipe(
                catchError(this.handleError<SupervisorInfo>('get supervisor info', null))
            );
    }

    public getAllProgramsInfo(cache: boolean = true): Observable<ProcessInfo[]> {
      if (cache) {
          return this.http.get<ProcessInfo[]>('/api/info/all')
              .pipe(
                  catchError(this.handleError<ProcessInfo[]>('get all processes info', null))
              );
      } else {
          return this.http.get<ProcessInfo[]>('/api/info/all', skipCache)
              .pipe(
                  catchError(this.handleError<ProcessInfo[]>('get all processes info', null))
              );
      }
    }

    public getProgramInfo(name: string): Observable<ProcessInfo> {
        return this.http.get<ProcessInfo>('/api/info/'+name, skipCache)
            .pipe(
                catchError(this.handleError<ProcessInfo>('get processes info', null))
            );
    }

    public getNodesList(): Observable<NodeList> {
        return this.http.get<NodeList>('/api/nodes')
            .pipe(
                catchError(this.handleError<NodeList>('get node list', null))
            )
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
