package dk.syslab.supv.dto.distributed;


import dk.syslab.supv.dto.supervisor.TailLog;

public class ResultTailLog extends ResultStatus {
    private TailLog log;

    public ResultTailLog() {}

    public ResultTailLog(String name, boolean success) {
        this(name, success, null);
    }

    public ResultTailLog(String name, boolean success, String message) {
        super(name, success, message);
    }

    public TailLog getLog() {
        return log;
    }

    public void setLog(TailLog log) {
        this.log = log;
    }
}
