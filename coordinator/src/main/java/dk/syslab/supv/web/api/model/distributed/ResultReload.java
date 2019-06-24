package dk.syslab.supv.web.api.model.distributed;

import java.util.List;
import java.util.Map;

public class ResultReload extends ResultStatus {
    private Map<String, List<String>> result;

    public ResultReload() {}

    public ResultReload(String name, boolean success) {
        this(name, success, null);
    }

    public ResultReload(String name, boolean success, String message) {
        super(name, success, message);
    }

    public Map<String, List<String>> getResult() {
        return result;
    }

    public void setResult(Map<String, List<String>> result) {
        this.result = result;
    }
}
