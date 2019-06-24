package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Result;
import dk.syslab.supv.dto.supervisor.ProcessStatus;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class CommandTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void startProgram() throws Exception {
        Result res = api.command().startProgram("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void stopProgram() throws Exception {
        Result res = api.command().stopProgram("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, true, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void startProgramGroup() throws Exception {
        List<ProcessStatus> res = api.command().startProgramGroup("localhost:9080", Util.TEST_NODE, "test", false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void stopProgramGroup() throws Exception {
        List<ProcessStatus> res = api.command().stopProgramGroup("localhost:9080", Util.TEST_NODE, "test", false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void signalProgram() throws Exception {
        Result res = api.command().signalProgram("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, "HUP", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void signalProgramGroup() throws Exception {
        List<ProcessStatus> res = api.command().signalProgramGroup("localhost:9080", Util.TEST_NODE, "test", "HUP", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void signalAllPrograms() throws Exception {
        List<ProcessStatus> res = api.command().signalAllPrograms("localhost:9080", Util.TEST_NODE, "HUP", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void update() throws Exception {
        Result res = api.command().update("localhost:9080", Util.TEST_NODE, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void sendMessage() throws Exception {
        Result res = api.command().sendMessage("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, "message", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void sendCommEvent() throws Exception {
        Result res = api.command().sendCommEvent("localhost:9080", Util.TEST_NODE, "type", "message", Util.TEST_TOKEN);
        assertNotNull(res);
    }

}
