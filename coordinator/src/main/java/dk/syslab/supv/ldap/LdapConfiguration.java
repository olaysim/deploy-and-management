package dk.syslab.supv.ldap;

public class LdapConfiguration {
    private String server;
    private int port;
    private String bindUser;
    private String bindPassword;
    private String baseDN;
    private String groupSearchBase;

    public LdapConfiguration() {}

    public LdapConfiguration(String server, int port, String bindUser, String bindPassword, String baseDN, String groupSearchBase) {
        this.server = server;
        this.port = port;
        this.bindUser = bindUser;
        this.bindPassword = bindPassword;
        this.baseDN = baseDN;
        this.groupSearchBase = groupSearchBase;
    }

    public String getServer() {
        return server;
    }

    public int getPort() {
        return port;
    }

    public String getBindUser() {
        return bindUser;
    }

    public String getBindPassword() {
        return bindPassword;
    }

    public String getBaseDN() {
        return baseDN;
    }

    public String getGroupSearchBase() {
        return groupSearchBase;
    }

    public String getFullGroupSearchBase() {
        return groupSearchBase + "," + baseDN;
    }
}
