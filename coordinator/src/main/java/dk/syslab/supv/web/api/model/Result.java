package dk.syslab.supv.web.api.model;

import dk.syslab.controller.rpc.protobuf.Messages;

public class Result {
    private boolean success;
    private String description;

    public Result() {}

    public Result(boolean success) {
        this.success = success;
    }

    public Result(boolean success, String message) {
        this.success = success;
        this.description = message;
    }

    public Result(Messages.Result result) {
        this.success = result.getSuccess();
        this.description = result.getMessage();
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "success: " + success;
    }
}
