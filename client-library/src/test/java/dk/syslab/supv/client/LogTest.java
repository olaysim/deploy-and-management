package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Result;
import dk.syslab.supv.dto.supervisor.ProcessStatus;
import dk.syslab.supv.dto.supervisor.TailLog;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

public class LogTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void getMainLog() throws Exception {
        String res = api.log().getLog("localhost:9080", Util.TEST_NODE, 0, 1000);
        assertNotNull(res);
    }

    @Test
    public void getProgramLog() throws Exception {
        String res = api.log().getLog("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, 0, 1000);
        assertNotNull(res);
    }

    @Test
    public void getProgramErrorLog() throws Exception {
        String res = api.log().getErrorLog("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, 0, 1000);
        assertNotNull(res);
    }

    @Test
    public void tailProgramLog() throws Exception {
        TailLog res = api.log().getTail("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, 0, 1000);
        assertNotNull(res);
    }

    @Test
    public void tailProgramErrorLog() throws Exception {
        TailLog res = api.log().getTailError("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, 0, 1000);
        assertNotNull(res);
    }

    @Test
    public void clearProgramLog() throws Exception {
        Result res = api.log().clearLog("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void clearAllProgramLogs() throws Exception {
        List<ProcessStatus> res = api.log().clearAllLogs("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }
}
