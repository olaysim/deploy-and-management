import {Observable} from "rxjs/Observable";

export class LogConfigurationAndState {
    public name: string;
    public offset = 0;
    public loadTime = 1000;
    public observable: Observable<string>;

    constructor(name: string) {
        this.name = name;
    }

    public update(offset: number) {
        this.offset = offset;
    }
}
