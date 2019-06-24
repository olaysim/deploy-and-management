package dk.syslab.supv.client;

import dk.syslab.supv.dto.AuthResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class AuthticateTest {

    @Test
    public void authenticate() throws Exception {

        String username = "";
        String password = "";

        SupvApi api = SupvClient.getApi();
        AuthResponse res = api.authenticate("localhost:9080", username, password);
        assertNotNull(res);
    }
}
