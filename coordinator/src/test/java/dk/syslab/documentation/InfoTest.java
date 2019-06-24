//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.web.api.InfoController;
//import dk.syslab.supv.rpc.model.xmlrpc.ProcessInfo;
//import dk.syslab.supv.rpc.model.xmlrpc.SupervisorInfo;
//import dk.syslab.supv.rpc.model.xmlrpc.XmlRpcService;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.restdocs.JUnitRestDocumentation;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.context.WebApplicationContext;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
//import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
//import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({InfoController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class InfoTest {
//    @Rule
//    public JUnitRestDocumentation restDocumentation = new JUnitRestDocumentation();
//
//    private MockMvc mockMvc;
//
//    @Autowired
//    private WebApplicationContext context;
//
//    @Autowired
//    Util util;
//
//    @Autowired
//    ObjectMapper mapper;
//
//    @Autowired
//    XmlRpcService supervisor;
//
//    @Before
//    public void setUp() throws IOException {
//        this.mockMvc = util.setUp(context, restDocumentation);
//    }
//
//    @Test
//    public void supervisorInfo() throws Exception {
//        SupervisorInfo info = new SupervisorInfo();
//        info.setState("RUNNING");
//        Mockito.when(supervisor.currentSupervisorInfo()).thenReturn(info);
//
//        this.mockMvc.perform(get("/api/info"))
//            .andExpect(status().isOk());
//    }
//
//    @Test
//    public void processInfo() throws Exception {
//        ProcessInfo info = new ProcessInfo();
//        info.setName("program");
//        info.setGroup("program");
//        info.setDescription("Program description");
//        Mockito.when(supervisor.getProcessInfo(Mockito.anyString())).thenReturn(info);
//
//        this.mockMvc.perform(get("/api/info/{name}", "program"))
//            .andExpect(status().isOk())
//            .andDo(document("process-info", pathParameters(
//                parameterWithName("name").description("The name of the program to get the process info from")
//            )));
//    }
//
//    @Test
//    public void allProcessInfo() throws Exception {
//        List<ProcessInfo> list = new ArrayList<>();
//        ProcessInfo info1 = new ProcessInfo();
//        info1.setName("program1");
//        info1.setGroup("program1");
//        info1.setDescription("Program 1 description");
//        list.add(info1);
//        ProcessInfo info2 = new ProcessInfo();
//        info2.setName("program2");
//        info2.setGroup("program2");
//        info2.setDescription("Program 2 description");
//        list.add(info2);
//
//        Mockito.when(supervisor.getAllProcessInfo()).thenReturn(list);
//
//        this.mockMvc.perform(get("/api/info/all"))
//            .andExpect(status().isOk());
//    }
//}
