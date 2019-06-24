import {Inject, Injectable} from '@angular/core';
import {DOCUMENT} from "@angular/platform-browser";

@Injectable()
export class ClipboardService {

    // see https://www.bennadel.com/blog/3235-creating-a-simple-copy-to-clipboard-directive-in-angular-2-4-9.htm
    // this tutorial also has a recipe for a copy to clipboard directive, could be useful...

    private dom: Document;

    constructor(@Inject(DOCUMENT) dom: Document) {
        this.dom = dom;
    }

    public copy(value: string) {
        var promise = new Promise((resolve, reject): void => {
                var textarea = null;
                try {
                    textarea = this.dom.createElement("textarea");
                    textarea.style.height = "0px";
                    textarea.style.left = "-100px";
                    textarea.style.opacity = "0";
                    textarea.style.position = "fixed";
                    textarea.style.top = "-100px";
                    textarea.style.width = "0px";
                    this.dom.body.appendChild(textarea);
                    textarea.value = value;
                    textarea.select();
                    this.dom.execCommand("copy");
                    resolve(value);
                } finally {
                    if (textarea && textarea.parentNode) {
                        textarea.parentNode.removeChild(textarea);
                    }
                }
            });
        return promise;
    }
}
