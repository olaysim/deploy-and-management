package dk.syslab.controller.validation;

public class ValidationException extends RuntimeException {
    private ValidationStatus status;

    private ValidationException() {
    }

    public ValidationException(ValidationStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ValidationException(ValidationStatus status, String message, Throwable cause) {
        super(message, cause);
        this.status = status;
    }

    public ValidationException(ValidationStatus status, Throwable cause) {
        super(cause);
        this.status = status;
    }

    public ValidationException(ValidationStatus status, String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
        this.status = status;
    }

    public ValidationStatus getStatus() {
        return status;
    }
}
