package dk.syslab.supv.client;

import dk.syslab.supv.Util;
import dk.syslab.supv.dto.Result;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class GroupTest {

    private SupvApi api;

    @Before
    public void setUp() {
        api = SupvClient.getApi();
    }

    @Test
    public void getGroups() throws Exception {
        Map<String, ArrayList<String>> res = api.group().getGroups("localhost:9080", Util.TEST_NODE);
        assertNotNull(res);
    }

    @Test
    public void addGroup() throws Exception {
        Result res = api.group().addProgramToGroup("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, "groupX", 888, Util.TEST_TOKEN);
        assertNotNull(res);
    }

    @Test
    public void removeGroup() throws Exception {
        Result res = api.group().deleteProgramFromGroup("localhost:9080", Util.TEST_NODE, Util.TEST_PROGRAM, "groupX", Util.TEST_TOKEN);
        assertNotNull(res);
    }

}
