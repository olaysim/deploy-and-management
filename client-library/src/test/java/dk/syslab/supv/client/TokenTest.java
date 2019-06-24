package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.TokenResponse;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class TokenTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void generateToken() throws Exception {
        TokenResponse res = api.generateToken("localhost:9080", 10, false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

}
