package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class AdminTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void shutdown() throws Exception {
        Result res = api.admin().shutdown("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void restart() throws Exception {
        Result res = api.admin().restart("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void clearlog() throws Exception {
        Result res = api.admin().clearlog("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void reload() throws Exception {
        Map<String, List<String>> res = api.admin().reload("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }
}
