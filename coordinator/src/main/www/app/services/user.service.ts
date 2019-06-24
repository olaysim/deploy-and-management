import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {MessageService} from "./message.service";
import {Observable} from "rxjs/Observable";
import {User} from "../lib/user";
import {catchError} from "rxjs/operators";
import {of} from "rxjs/observable/of";
import {Login} from "../lib/login";
import {Subject} from "rxjs/Subject";

const skipCache = {headers: new HttpHeaders({'disable-cache':  '1'})};

@Injectable()
export class UserService {
    private authUrl = '/api/auth';
    private user: User;
    private loaded: boolean; // is user loaded from local storage

    constructor(
        private http: HttpClient,
        private messageService: MessageService
    ) {}

    // service methods
    isLoggedIn(): boolean {
        if (!this.loaded) {
            this.loaded = true;
            this.user = this.loadUser();
        }

        // check that the token has not expired
        if (this.user != null) {
            let now = new Date().valueOf();
            if (now > this.user.expiresAt) {
                this.removeUser(); // token expired, clean up
            }
        }
        return this.user != null;
    }

    login(login: Login): Observable<boolean> {
        let result = new Subject<boolean>();
        this.authenticate(login)
            .subscribe(
                user => {
                    if (user != null) {
                        this.saveUser(user);
                        this.user = user;
                    }
                    result.next(this.isLoggedIn());
                    result.complete();
                });
        return result;
    }

    logout() {
        this.user = null;
        this.removeUser();
    }

    getDisplayName() {
        if (this.user == null) return null;
        return this.user.name;
    }

    getEmail() {
        if (this.user == null) return null;
        return this.user.email;
    }

    getToken() {
        if (this.user == null) return null;
        return this.user.token;
    }

    // api methods
    private authenticate(login: Login): Observable<User> {
        return this.http.post<User>(this.authUrl, login, skipCache)
            .pipe(
                catchError(this.handleError<User>('authenticate', null))
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
                this.messageService.error('Login failed');
            }

            // Let the app keep running by returning an empty result.
            return of(result as T);
        };
    }

    private loadUser(): User {
        let user = new User();
        user.name = localStorage.getItem('username');
        if (user.name) {
            user.email = localStorage.getItem('email');
            user.expiresAt = parseInt(localStorage.getItem('expiresAt'));
            user.token = localStorage.getItem('token');
            return user;
        } else {
            return null;
        }
    }

    private saveUser(user: User) {
        localStorage.setItem('username', user.name);
        localStorage.setItem('email', user.email);
        localStorage.setItem('expiresAt', user.expiresAt.toString());
        localStorage.setItem('token', user.token);
    }

    private removeUser() {
        localStorage.removeItem('username');
        localStorage.removeItem('email');
        localStorage.removeItem('expiresAt');
        localStorage.removeItem('token');
    }
}
