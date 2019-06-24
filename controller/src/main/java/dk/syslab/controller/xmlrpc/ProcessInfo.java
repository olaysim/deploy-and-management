package dk.syslab.controller.xmlrpc;

public class ProcessInfo {
    private String name;
    private String group;
    private String description;
    private int start;
    private int stop;
    private int now;
    private String statename;
    private int state;
    private String spawnerr;
    private int exitstatus;
    private String logfile;
    private String stdOutLogfile;
    private String stdErrLogfile;
    private int pid;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getStop() {
        return stop;
    }

    public void setStop(int stop) {
        this.stop = stop;
    }

    public int getNow() {
        return now;
    }

    public void setNow(int now) {
        this.now = now;
    }

    public String getStatename() {
        return statename;
    }

    public void setStatename(String statename) {
        this.statename = statename;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getSpawnerr() {
        return spawnerr;
    }

    public void setSpawnerr(String spawnerr) {
        this.spawnerr = spawnerr;
    }

    public int getExitstatus() {
        return exitstatus;
    }

    public void setExitstatus(int exitstatus) {
        this.exitstatus = exitstatus;
    }

    public String getLogfile() {
        return logfile;
    }

    public void setLogfile(String logfile) {
        this.logfile = logfile;
    }

    public String getStdOutLogfile() {
        return stdOutLogfile;
    }

    public void setStdOutLogfile(String stdOutLogfile) {
        this.stdOutLogfile = stdOutLogfile;
    }

    public String getStdErrLogfile() {
        return stdErrLogfile;
    }

    public void setStdErrLogfile(String stdErrLogfile) {
        this.stdErrLogfile = stdErrLogfile;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}

