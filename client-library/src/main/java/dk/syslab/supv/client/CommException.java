package dk.syslab.supv.client;

import java.io.IOException;

/**
 * This class is an extension to the IOException and has an extra field to describe the error.
 * The error message originates from the REST server (based on spring boot)
 */
public class CommException extends IOException {
    private int code;
    private String reason;
    private String message;

    public CommException() {
        super();
    }

    public CommException(String message) {
        super(message);
    }

    public CommException(String message, Throwable cause) {
        super(message, cause);
    }

    public CommException(Throwable cause) {
        super(cause);
    }

    public CommException(int code, String reason) {
        super(code + " - " + reason);
        this.code = code;
        this.reason = reason;
    }

    public CommException(int code, String reason, String message) {
        super(code + " - " + reason + ": " + message);
        this.code = code;
        this.reason = reason;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        if (message != null)
            return code + " - " + reason + ": " + message;
        else
            return code + " - " + reason;
    }
}
