package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Program;
import dk.syslab.supv.dto.Result;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class ProcessTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void uploadProgram() throws Exception {
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

        Result res = api.program().uploadProgram("localhost:9080", Util.TEST_NODE, p, files, paths, transforms, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void deleteProgram() throws Exception {
        Result res = api.program().deleteProgram("localhost:9080", Util.TEST_NODE, "uploadtest1", Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void listProgramFiles() throws Exception {
        List<String> res = api.program().listProgramFiles("localhost:9080", Util.TEST_NODE, "uploadtest1", Util.TEST_TOKEN);
        assertNotNull(res);
    }
}
