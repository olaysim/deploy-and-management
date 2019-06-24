package dk.syslab.supv.dto.distributed;

public class ResultStatus {
    private String name;
    private boolean success;
    private String description;

    public ResultStatus() {}

    public ResultStatus(String name, boolean success) {
        this(name, success, null);
    }

    public ResultStatus(String name, boolean success, String description) {
        this.success = success;
        this.description = description;
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
