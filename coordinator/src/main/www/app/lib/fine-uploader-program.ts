export class FineUploaderProgram {
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

    public uuidFilenames: object = new Object();
    public uuidPaths: object = new Object();
    public paths: object = new Object();
    public transforms: object = new Object();

    public transaction: string;

    public movedata: boolean;
}
