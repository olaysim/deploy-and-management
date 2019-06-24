import { Injectable } from '@angular/core';
import {HttpRequest, HttpResponse} from "@angular/common/http";

// This Request Cache Service is used by the CachingInterceptor
// This code follows the official guide https://angular.io/guide/http
// The code can be found here
//     https://github.com/angular/angular/blob/db3e65fb1786179930704868f578429e47c399bf/aio/content/examples/http/src/app/http-interceptors/caching-interceptor.ts
//     https://github.com/angular/angular/blob/db3e65fb1786179930704868f578429e47c399bf/aio/content/examples/http/src/app/request-cache.service.ts

export interface RequestCacheEntry {
    url: string;
    response: HttpResponse<any>;
    lastRead: number;
}

export abstract class RequestCache {
    abstract get(req: HttpRequest<any>): HttpResponse<any> | undefined;
    abstract put(req: HttpRequest<any>, response: HttpResponse<any>): void
}

const maxAge = 30000; // maximum cache age (ms)

@Injectable()
export class RequestCacheWithMap implements RequestCache {
    cache = new Map<string, RequestCacheEntry>();

    constructor() { }

    get(req: HttpRequest<any>): HttpResponse<any> | undefined {
        const url = req.urlWithParams;
        const cached = this.cache.get(url);
        if (!cached) {
            return undefined;
        }
        return (cached.lastRead < (Date.now() - maxAge)) ? undefined : cached.response;
    }

    put(req: HttpRequest<any>, response: HttpResponse<any>): void {
        const url = req.urlWithParams;
        const entry = { url, response, lastRead: Date.now() };
        this.cache.set(url, entry);

        // remove expired cache entries
        const expired = Date.now() - maxAge;
        this.cache.forEach(entry => {
            if (entry.lastRead < expired) {
                this.cache.delete(entry.url);
            }
        });
    }
}
