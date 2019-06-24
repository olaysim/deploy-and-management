package dk.syslab.supv.dto;

public class Program {
    public final static int       DEFAULT_PRIORITY = 999;
    public final static boolean  DEFAULT_AUTOSTART = true;
    public final static String DEFAULT_AUTORESTART = "unexpected"; // false, unexpected, true
    public final static int      DEFAULT_STARTSECS = 1;
    public final static int   DEFAULT_STARTRETRIES = 3;
    public final static String   DEFAULT_EXITCODES = "0,2";
    public final static int   DEFAULT_STOPWAITSECS = 10;

    private String name        = null;
    private String command     = null;
    private Integer priority   = null;
    private Boolean autostart  = null;
    private String autorestart = null;
    private Integer startsecs  = null;
    private Integer startretries = null;
    private String exitcodes   = null;
    private Integer stopwaitsecs = null;
    private String environment = null;
    // directory and user is set by application
    // more options are available, see documentation

    public boolean isValidProgramConfiguration() {
        return name != null && !name.isEmpty() &&
            command != null && !command.isEmpty();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Boolean getAutostart() {
        return autostart;
    }

    public void setAutostart(Boolean autostart) {
        this.autostart = autostart;
    }

    public String getAutorestart() {
        return autorestart;
    }

    public void setAutorestart(String autorestart) {
        this.autorestart = autorestart;
    }

    public Integer getStartsecs() {
        return startsecs;
    }

    public void setStartsecs(Integer startsecs) {
        this.startsecs = startsecs;
    }

    public Integer getStartretries() {
        return startretries;
    }

    public void setStartretries(Integer startretries) {
        this.startretries = startretries;
    }

    public String getExitcodes() {
        return exitcodes;
    }

    public void setExitcodes(String exitcodes) {
        this.exitcodes = exitcodes;
    }

    public Integer getStopwaitsecs() {
        return stopwaitsecs;
    }

    public void setStopwaitsecs(Integer stopwaitsecs) {
        this.stopwaitsecs = stopwaitsecs;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
