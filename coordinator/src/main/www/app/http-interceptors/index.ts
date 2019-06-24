import { HTTP_INTERCEPTORS } from '@angular/common/http';
import {AuthInterceptor} from "./auth-interceptor";
import {CachingInterceptor} from "./caching-interceptor";

export const httpInterceptorProviders = [
    { provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true },
    { provide: HTTP_INTERCEPTORS, useClass: CachingInterceptor, multi: true },
];
