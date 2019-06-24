package dk.syslab.supv.dto.supervisor;

public class SupervisorState {
    public final static int FATAL = 2;
    public final static int RUNNING = 1;
    public final static int RESTARTING = 0;
    public final static int SHUTDOWN = -1;
}
