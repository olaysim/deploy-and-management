//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.CommandController;
//import dk.syslab.supv.rpc.model.xmlrpc.XmlRpcService;
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.impl.DefaultClaims;
//import org.junit.Before;
//import org.junit.Rule;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.mockito.Mockito;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.http.MediaType;
//import org.springframework.restdocs.JUnitRestDocumentation;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.context.junit4.SpringRunner;
//import org.springframework.test.web.servlet.MockMvc;
//import org.springframework.web.context.WebApplicationContext;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({CommandController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class CommandTest {
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
//    @Autowired
//    Validator validator;
//
//    @Before
//    public void setUp() throws IOException {
//        this.mockMvc = util.setUp(context, restDocumentation);
//
//        Mockito.when(validator.validate(Mockito.any(String.class), Mockito.any(HttpServletResponse.class))).thenReturn(new DefaultClaims());
//        Mockito.when(validator.isAdmin(Mockito.any(Claims.class))).thenReturn(false);
//    }
//
//    @Test
//    public void startProgram() throws Exception {
//        Mockito.when(supervisor.startProcess("program", false)).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/start/{name}?wait=0", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("start-program", pathParameters(
//                parameterWithName("name").description("The name of the program to start")
//            ), requestParameters(
//                parameterWithName("wait").description("Wait for operation to finish: 0 = no, 1 = yes. If not included the default is 0.")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void startProgramGroup() throws Exception {
//        Mockito.when(supervisor.startProcessGroup("groupX", false)).thenReturn(util.getProcessStatus());
//
//        this.mockMvc.perform(post("/api/start/group/{name}?wait=0", "groupX").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("start-program-group", pathParameters(
//                parameterWithName("name").description("The name of the group (of programs) to start")
//            ), requestParameters(
//                parameterWithName("wait").description("Wait for operation to finish: 0 = no, 1 = yes. If not included the default is 0.")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//
//    @Test
//    public void stopProgram() throws Exception {
//        Mockito.when(supervisor.stopProcess("program", false)).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/stop/{name}?wait=0", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("stop-program", pathParameters(
//                parameterWithName("name").description("The name of the program to stop")
//            ), requestParameters(
//                parameterWithName("wait").description("Wait for operation to finish: 0 = no, 1 = yes. If not included the default is 0.")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ),  responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void stopProgramGroup() throws Exception {
//        Mockito.when(supervisor.stopProcessGroup("groupX", false)).thenReturn(util.getProcessStatus());
//
//        this.mockMvc.perform(post("/api/stop/group/{name}?wait=0", "groupX").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("stop-program-group", pathParameters(
//                parameterWithName("name").description("The name of the group (of programs) to stop")
//            ), requestParameters(
//                parameterWithName("wait").description("Wait for operation to finish: 0 = no, 1 = yes. If not included the default is 0.")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//
//    @Test
//    public void signalProgram() throws Exception {
//        Mockito.when(supervisor.signalProcess("program", "signal")).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/signal/{name}/{signal}", "program", "signal").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("signal-program", pathParameters(
//                parameterWithName("name").description("The name of the program to send the signal to"),
//                parameterWithName("signal").description("The signal (string) to send")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void signalAllPrograms() throws Exception {
//        Mockito.when(supervisor.signalAllProcesses("signal")).thenReturn(util.getProcessStatus());
//
//        this.mockMvc.perform(post("/api/signal/all/{signal}", "signal").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("signal-all-programs", pathParameters(
//                parameterWithName("signal").description("The signal (string) to send")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//
//    @Test
//    public void signalProgramGroup() throws Exception {
//        Mockito.when(supervisor.signalProcessGroup("groupX", "signal")).thenReturn(util.getProcessStatus());
//
//        this.mockMvc.perform(post("/api/signal/group/{name}/{signal}", "groupX", "signal").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("signal-program-group", pathParameters(
//                parameterWithName("name").description("The name of the group (of programs) to stop"),
//                parameterWithName("signal").description("The signal (string) to send")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//
//    @Test
//    public void update() throws Exception {
//        Mockito.when(supervisor.update()).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/update").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("update", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                    fieldWithPath("success").description("Describes whether the operation was a success"),
//                    fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void sendMessage() throws Exception {
//        Map<String, Object> body = new HashMap<>();
//        body.put("data", "<message>");
//
//        Mockito.when(supervisor.sendProcessStdin("program", "<message>")).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/send/{name}", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON)
//            .content(mapper.writeValueAsString(body)))
//            .andExpect(status().isOk())
//            .andDo(document("send-message", pathParameters(
//                parameterWithName("name").description("The name of the program to send the signal to")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), requestFields(
//                fieldWithPath("data").description("The message to send to the program (message is a string)")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void sendCommEvent() throws Exception {
//        Map<String, Object> body = new HashMap<>();
//        body.put("type", "<type>");
//        body.put("data", "<data>");
//
//        Mockito.when(supervisor.sendRemoteCommEvent("<type>", "<data>")).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/sendcomm").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON)
//            .content(mapper.writeValueAsString(body)))
//            .andExpect(status().isOk())
//            .andDo(document("send-comm-event", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), requestFields(
//                fieldWithPath("type").description("The type to send to the supervisor remote comm interface (type is a string)"),
//                fieldWithPath("data").description("The data to send to the supervisor remote comm interface (data is a string)")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//}
