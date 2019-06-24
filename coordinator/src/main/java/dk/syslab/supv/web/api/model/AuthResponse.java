package dk.syslab.supv.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class AuthResponse {
    private String authToken;
    private String name;
    private String email;
    private long expiresAt;

    public AuthResponse(String authToken, String name, String email, Date expiresAt) {
        this.authToken = authToken;
        this.name = name;
        this.email = email;
        this.expiresAt = expiresAt.getTime();
    }

    @JsonProperty("token")
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }
}
