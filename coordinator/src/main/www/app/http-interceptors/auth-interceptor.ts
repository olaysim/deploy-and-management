import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {UserService} from "../services/user.service";

@Injectable()
export class AuthInterceptor implements HttpInterceptor {

    // adds 'application/json' header to all http calls
    // adds authorization token if available

    constructor(
        private userService: UserService
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        const token = this.userService.getToken();
        let clonedReq: HttpRequest<any>;

        // add 'application/json' header
        // but only if 'disable-header' has not been set
        if (!req.headers.get('disable-header')) {
            clonedReq = req.clone({setHeaders: {'Content-Type': 'application/json'}});
        }

        // add authentication token if set
        if (!req.headers.get('disable-auth') && token != null) {
            if (clonedReq == null) {
                clonedReq = req.clone({setHeaders: {'Authorization': 'Bearer ' + token}});
            } else {
                clonedReq = clonedReq.clone({setHeaders: {'Authorization': 'Bearer ' + token}});
            }
        }

        if (clonedReq == null) {
            return next.handle(req);
        } else {
            return next.handle(clonedReq);
        }
    }

}
