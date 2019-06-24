package dk.syslab.supv.dto.distributed;

import java.util.ArrayList;
import java.util.HashMap;

public class ResultReload extends ResultStatus {
    private HashMap<String, ArrayList<String>> result;

    public ResultReload() {}

    public ResultReload(String name, boolean success) {
        this(name, success, null);
    }

    public ResultReload(String name, boolean success, String message) {
        super(name, success, message);
    }

    public HashMap<String, ArrayList<String>> getResult() {
        return result;
    }

    public void setResult(HashMap<String, ArrayList<String>> result) {
        this.result = result;
    }
}
