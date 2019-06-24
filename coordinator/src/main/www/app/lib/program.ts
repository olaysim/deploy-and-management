export class Program {
    public name: string = '';
    public command: string = '';
    public priority: number;
    public autostart: boolean = true;
    public autorestart: string = '';
    public startsecs: number;
    public startretries: number;
    public exitcodes: string = '';
    public stopwaitsecs: number;
    public environment: string = '';

    public files: string[];
    public paths: string[];
    public transforms: string[];
}
