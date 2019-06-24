export class MessageModel {
    public message: string;
    public messageType: string;
    public appendNewline: boolean;

    constructor(newline: boolean = false) {
        this.appendNewline = newline;
    }
}
