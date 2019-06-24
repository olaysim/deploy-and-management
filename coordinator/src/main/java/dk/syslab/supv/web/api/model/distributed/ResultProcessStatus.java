package dk.syslab.supv.web.api.model.distributed;

import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;

import java.util.List;

public class ResultProcessStatus extends ResultStatus {
    private List<ProcessStatus> processStatus;

    public ResultProcessStatus() {}

    public ResultProcessStatus(String name, boolean success) {
        this(name, success, null);
    }

    public ResultProcessStatus(String name, boolean success, String message) {
        super(name, success, message);
    }

    public List<ProcessStatus> getProcessStatus() {
        return processStatus;
    }

    public void setProcessStatus(List<ProcessStatus> processStatus) {
        this.processStatus = processStatus;
    }
}
