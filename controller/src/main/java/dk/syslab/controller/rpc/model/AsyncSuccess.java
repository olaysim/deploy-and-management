package dk.syslab.controller.rpc.model;

public class AsyncSuccess {
    private boolean success;
    private String message;

    public AsyncSuccess() { }

    public AsyncSuccess(boolean success) {
        this.success = success;
    }

    public AsyncSuccess(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
