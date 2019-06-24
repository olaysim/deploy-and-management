package dk.syslab.supv.web.api.model;

import java.util.Map;

public class FineUploaderProgram {
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

    private Map<String, String> uuidFilenames;
    private Map<String, String> uuidPaths;
    private Map<String, String> paths;
    private Map<String, String> transforms;

    private String transaction;

    private boolean movedata;

    public FineUploaderProgram() { }

    public FineUploaderProgram(Program program) {
        this.name = program.getName();
        this.command = program.getCommand();
        this.priority = program.getPriority();
        this.autostart = program.getAutostart();
        this.autorestart = program.getAutorestart();
        this.startsecs = program.getStartsecs();
        this.startretries = program.getStartretries();
        this.exitcodes = program.getExitcodes();
        this.stopwaitsecs = program.getStopwaitsecs();
        this.environment = program.getEnvironment();
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

    public Map<String, String> getUuidFilenames() {
        return uuidFilenames;
    }

    public void setUuidFilenames(Map<String, String> uuidFilenames) {
        this.uuidFilenames = uuidFilenames;
    }

    public Map<String, String> getUuidPaths() {
        return uuidPaths;
    }

    public void setUuidPaths(Map<String, String> uuidPaths) {
        this.uuidPaths = uuidPaths;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, String> paths) {
        this.paths = paths;
    }

    public Map<String, String> getTransforms() {
        return transforms;
    }

    public void setTransforms(Map<String, String> transforms) {
        this.transforms = transforms;
    }

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public boolean isMovedata() {
        return movedata;
    }

    public void setMovedata(boolean movedata) {
        this.movedata = movedata;
    }
}
