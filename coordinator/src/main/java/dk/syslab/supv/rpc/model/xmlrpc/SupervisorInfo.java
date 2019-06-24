package dk.syslab.supv.rpc.model.xmlrpc;

public class SupervisorInfo {
    private String apiVersion = "api ver";
    private String packageVersion = "pack ver";
    private String identifier = "";
    private String state = "UNKNOWN";
    private int code = 0;
    private int pid = 0;

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
    }

    public String getPackageVersion() {
        return packageVersion;
    }

    public void setPackageVersion(String packageVersion) {
        this.packageVersion = packageVersion;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }
}
