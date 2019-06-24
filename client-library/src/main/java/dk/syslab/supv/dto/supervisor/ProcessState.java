package dk.syslab.supv.dto.supervisor;

public class ProcessState {
    public final static int STOPPED = 0;
    public final static int STARTING = 10;
    public final static int RUNNING = 20;
    public final static int BACKOFF = 30;
    public final static int STOPPING = 40;
    public final static int EXITED = 100;
    public final static int FATAL = 200;
    public final static int UNKNOWN = 1000;
}
