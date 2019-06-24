package dk.syslab.supv.dto;

import java.util.Objects;

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
        if (description != null) {
            return "success: " + success + ", description: " + description;
        } else {
            return "success: " + success;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Result)) return false;
        Result result = (Result) o;
        return success == result.success &&
                Objects.equals(description, result.description);
    }

    @Override
    public int hashCode() {

        return Objects.hash(success, description);
    }
}
