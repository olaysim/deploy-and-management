package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.supervisor.ProcessInfo;
import dk.syslab.supv.dto.supervisor.SupervisorInfo;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class InfoTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void supervisorInfo() throws Exception {
        SupervisorInfo res = api.info().getSupervisorInfo("localhost:9080", Util.TEST_NODE);
        assertNotNull(res);
    }

    @Test
    public void processInfo() throws Exception {
        ProcessInfo res = api.info().getProcessInfo("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM);
        assertNotNull(res);
    }

    @Test
    public void allProcessInfo() throws Exception {
        List<ProcessInfo> res = api.info().getAllProcessInfo("localhost:9080", Util.TEST_NODE);
        assertNotNull(res);
    }
}
