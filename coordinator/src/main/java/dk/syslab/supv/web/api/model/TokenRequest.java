package dk.syslab.supv.web.api.model;

public class TokenRequest {
    private boolean admin;
    private int days;

    public TokenRequest() {
        this.admin = false;
        this.days = 30;
    }

    public boolean getAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public int getDays() {
        return days;
    }

    public void setDays(int days) {
        this.days = days;
    }
}
