package dk.syslab.deploy;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.List;
import java.util.Map;

public class Configuration {
    public final static int       DEFAULT_PRIORITY = 999;
    public final static boolean  DEFAULT_AUTOSTART = true;
    public final static String DEFAULT_AUTORESTART = "unexpected"; // false, unexpected, true
    public final static int      DEFAULT_STARTSECS = 1;
    public final static int   DEFAULT_STARTRETRIES = 3;
    public final static String   DEFAULT_EXITCODES = "0,2";
    public final static int   DEFAULT_STOPWAITSECS = 10;

    private String token       = null;
    private String name        = null;
    private String command     = null;
    private List<String> files = null;
    private Map<String, String> folders = null;
    private Map<String, String> paths   = null;
    private Map<String, Map<String, String>> transforms = null;
    private Integer priority   = null;
    private Boolean autostart  = null;
    private String autorestart = null;
    private Integer startsecs  = null;
    private Integer startretries = null;
    private String exitcodes   = null;
    private Integer stopwaitsecs = null;
    private String environment = null;

    private List<String> nodes = null;
    private String host        = null;

    @JsonIgnore
    public boolean isValidUpload() {
        return command != null && !command.isEmpty() && isValidCommand();
    }

    @JsonIgnore
    public boolean isValidCommand() {
        return token != null && !token.isEmpty() &&
                name != null && !name.isEmpty() &&
                nodes != null && nodes.size() > 0 &&
                host != null && !host.isEmpty();
    }

    @JsonIgnore
    public String getInvalidReasonForUpload() {
        if (command == null || command.isEmpty()) return "run-command is not set";
        return "";
    }

    @JsonIgnore
    public String getInvalidReasonForCommand() {
        if (token == null || token.isEmpty()) return "token is not set";
        if (name == null || name.isEmpty()) return "program name is not set";
        if (nodes == null || nodes.isEmpty()) return "list of nodes is not set";
        if (host == null || host.isEmpty()) return "host address is not set";
        return "";
    }


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public Map<String, String> getFolders() {
        return folders;
    }

    public void setFolders(Map<String, String> folders) {
        this.folders = folders;
    }

    public Map<String, String> getPaths() {
        return paths;
    }

    public void setPaths(Map<String, String> paths) {
        this.paths = paths;
    }

    public Map<String, Map<String, String>> getTransforms() {
        return transforms;
    }

    public void setTransforms(Map<String, Map<String, String>> transforms) {
        this.transforms = transforms;
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

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }
}
