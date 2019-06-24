package dk.syslab.supv.web.api.model.distributed;

import dk.syslab.supv.rpc.model.xmlrpc.ProcessInfo;

public class ResultProcessInfo extends ResultStatus {
    private ProcessInfo processInfo;

    public ResultProcessInfo() {}

    public ResultProcessInfo(String name, boolean success) {
        this(name, success, null);
    }

    public ResultProcessInfo(String name, boolean success, String message) {
        super(name, success, message);
    }

    public ProcessInfo getProcessInfo() {
        return processInfo;
    }

    public void setProcessInfo(ProcessInfo processInfo) {
        this.processInfo = processInfo;
    }
}
