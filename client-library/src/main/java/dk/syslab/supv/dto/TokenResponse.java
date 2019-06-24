package dk.syslab.supv.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    private String authToken;

    public TokenResponse() {}

    public TokenResponse(String authToken) {
        this.authToken = authToken;
    }

    @JsonProperty("token")
    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }
}
