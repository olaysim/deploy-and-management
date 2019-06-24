import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {of} from "rxjs/observable/of";
import {Observable} from "rxjs/Observable";
import {Result} from "../lib/result";
import {SendData} from "../lib/send-data";
import {catchError} from "rxjs/operators";
import {TokenModel} from "../lib/token-model";
import {TokenReply} from "../lib/token-reply";

const skipCache = {headers: new HttpHeaders({'disable-cache':  '1'})};

@Injectable()
export class TokenService {

  constructor(
      private http: HttpClient,
      private messageService: MessageService
  ) { }

    public generateToken(tokenModel: TokenModel): Observable<TokenReply> {
        return this.http.post<TokenReply>('/api/token', tokenModel, skipCache)
            .pipe(
                catchError(this.handleError<TokenReply>('generate token', null))
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
