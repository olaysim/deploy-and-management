import {HttpEvent, HttpHandler, HttpHeaders, HttpInterceptor, HttpRequest, HttpResponse} from "@angular/common/http";
import {Injectable} from "@angular/core";
import {Observable} from "rxjs/Observable";
import {RequestCache} from "../services/request-cache.service";
import {startWith, tap} from "rxjs/operators";
import {of} from "rxjs/observable/of";

@Injectable()
export class CachingInterceptor implements HttpInterceptor {

    // This interceptor follows the official guide
    // see https://angular.io/guide/http
    // The code can be found here
    //     https://github.com/angular/angular/blob/db3e65fb1786179930704868f578429e47c399bf/aio/content/examples/http/src/app/http-interceptors/caching-interceptor.ts
    //     https://github.com/angular/angular/blob/db3e65fb1786179930704868f578429e47c399bf/aio/content/examples/http/src/app/request-cache.service.ts


    constructor(
        private cache: RequestCache
    ) {}

    intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
        if (req.headers.get('disable-cache')) {
            return next.handle(req);
        }

        const cachedResponse = this.cache.get(req);

        if (req.headers.get('x-refresh')) {
            const results$ = sendRequest(req, next, this.cache);
            return cachedResponse ? results$.pipe( startWith(cachedResponse) ) : results$;
        }
        return cachedResponse ? of(cachedResponse) : sendRequest(req, next, this.cache);
    }
}

function sendRequest(
    req: HttpRequest<any>,
    next: HttpHandler,
    cache: RequestCache): Observable<HttpEvent<any>> {

    return next.handle(req).pipe(
        tap(event => {
            if (event instanceof HttpResponse) {
                cache.put(req, event);
            }
        })
    );
}
