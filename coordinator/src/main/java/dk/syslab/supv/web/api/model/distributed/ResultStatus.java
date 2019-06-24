package dk.syslab.supv.web.api.model.distributed;

public class ResultStatus {
    private String name;
    private boolean success;
    private String description;

    public ResultStatus() {}

    public ResultStatus(String name, boolean success) {
        this(name, success, null);
    }

    public ResultStatus(String name, boolean success, String message) {
        this.success = success;
        this.description = message;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
}
