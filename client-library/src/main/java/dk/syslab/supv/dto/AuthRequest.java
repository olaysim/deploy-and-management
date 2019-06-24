package dk.syslab.supv.dto;

public class AuthRequest {
    public String username;
    public char[] password;

    public AuthRequest() {}

    public AuthRequest(String username, String password) {
        this.username = username;
        this.password = password.toCharArray();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public char[] getPassword() {
        return password;
    }

    public void setPassword(char[] password) {
        this.password = password;
    }

    public boolean isReady() {
        return (username != null && password != null);
    }

    public void erasePassword() {
        if (password != null) {
            for (int i = 0; i < password.length; i++) {
                password[i] = '0';
            }
            password = null;
        }
    }
}
