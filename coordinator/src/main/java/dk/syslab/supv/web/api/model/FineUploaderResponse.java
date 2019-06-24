package dk.syslab.supv.web.api.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FineUploaderResponse {

    @JsonProperty("error")
    private String errorMsg;

    private boolean success;

    public FineUploaderResponse(boolean success) {
        this.success = success;
    }

    public FineUploaderResponse(boolean success, String errorMsg) {
        this.errorMsg = errorMsg;
        this.success = success;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }
}
