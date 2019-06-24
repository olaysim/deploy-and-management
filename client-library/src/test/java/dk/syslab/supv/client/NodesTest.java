package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Program;
import dk.syslab.supv.dto.distributed.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

@SuppressWarnings("Duplicates")
public class NodesTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void getNodes() throws Exception {
        NodeList res = api.nodes().getNodes("localhost:9080");
        assertNotNull(res);
    }

    @Test
    public void startProgramOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().startProgram("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void stopProgramOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().stopProgram("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void startProgramGroupOnNodes() throws Exception {
        Map<String, ResultProcessStatus> res = api.nodes().startProgramGroup("localhost:9080", Util.TEST_NODES, "test", false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void stopProgramGroupOnNodes() throws Exception {
        Map<String, ResultProcessStatus> res = api.nodes().stopProgramGroup("localhost:9080", Util.TEST_NODES, "test", false, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void processInfoOnNodes() throws Exception {
        Map<String, ResultProcessInfo> res = api.nodes().getProcessInfo("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void uploadProgramOnNodes() throws Exception {
        Program p = new Program();
        p.setName("uploadtest1");
        p.setCommand("test");

        List<File> files = new ArrayList<>();
        files.add(Paths.get("C:\\Users\\user\\Documents\\test\\incap ip addresser.txt").toFile());
        files.add(Paths.get("C:\\Users\\user\\Documents\\test\\tekst til wiki ang vlan timeline.txt").toFile());

        List<String> paths = new ArrayList<>();
        paths.add("incap ip addresser.txt#testdir\\haps");

        List<String> transforms = new ArrayList<>();
        transforms.add("syslab-01#testrename.txt#testnewname.txt");

        Map<String, ResultStatus> res = api.nodes().uploadProgram("localhost:9080", Util.TEST_NODES, p, files, paths, transforms, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void deleteProgramOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().deleteProgram("localhost:9080", Util.TEST_NODES, "uploadtest1", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void signalProgramOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().signalProgram("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, "HUP", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void sendMessageOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().sendMessage("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, "message", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void sendCommEventOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().sendCommEvent("localhost:9080", Util.TEST_NODES, "type", "message", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void restartNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().restart("localhost:9080", Util.TEST_NODES, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void clearProgramLogOnNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().clearLog("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void reloadNodes() throws Exception {
        Map<String, ResultStatus> res = api.nodes().update("localhost:9080", Util.TEST_NODES, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void tailProgramLogOnNodes() throws Exception {
        Map<String, ResultTailLog> res = api.nodes().getTail("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, 0, 1000, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void tailProgramErrorLogOnNodes() throws Exception {
        Map<String, ResultTailLog> res = api.nodes().getTailError("localhost:9080", Util.TEST_NODES, Util.TEST_PROGRAM, 0, 1000, Util.TEST_TOKEN);
        assertNotNull(res);
    }

}
