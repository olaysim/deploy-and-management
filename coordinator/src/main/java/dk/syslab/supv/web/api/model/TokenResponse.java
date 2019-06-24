package dk.syslab.supv.web.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TokenResponse {
    @JsonProperty("token")
    public String authToken;

    public TokenResponse(String authToken) {
        this.authToken = authToken;
    }
}
