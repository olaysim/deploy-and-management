//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.LogController;
//import dk.syslab.supv.rpc.model.xmlrpc.TailLog;
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
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({LogController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class LogTest {
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
//    public void mainLog() throws Exception {
//        Mockito.when(supervisor.readLog(Mockito.anyInt(), Mockito.anyInt())).thenReturn("log log log log log log log log log");
//
//        this.mockMvc.perform(get("/api/log?offset=500&length=1000"))
//            .andExpect(status().isOk())
//            .andDo(document("main-log", requestParameters(
//                parameterWithName("offset").description("Offset to read from"),
//                parameterWithName("length").description("Length of log to read")
//            )));
//    }
//
//    @Test
//    public void programLog() throws Exception {
//        Mockito.when(supervisor.readProcessStdoutLog(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn("log log log log log log log log log");
//
//        this.mockMvc.perform(get("/api/log/{name}?offset=500&length=1000", "program"))
//            .andExpect(status().isOk())
//            .andDo(document("program-log", pathParameters(
//                parameterWithName("name").description("The name of the program to read the log from")
//            ), requestParameters(
//                parameterWithName("offset").description("Offset to read from"),
//                parameterWithName("length").description("Length of log to read")
//            )));
//    }
//
//    @Test
//    public void programErrorLog() throws Exception {
//        Mockito.when(supervisor.readProcessStderrLog(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn("log log log log log log log log log");
//
//        this.mockMvc.perform(get("/api/log/err/{name}?offset=500&length=1000", "program"))
//            .andExpect(status().isOk())
//            .andDo(document("program-error-log", pathParameters(
//                parameterWithName("name").description("The name of the program to read the log from")
//            ), requestParameters(
//                parameterWithName("offset").description("Offset to read from"),
//                parameterWithName("length").description("Length of log to read")
//            )));
//    }
//
//    @Test
//    public void tailProgramLog() throws Exception {
//        TailLog log = new TailLog();
//        log.setOverflow(true);
//        log.setOffset(500);
//        log.setLog("log log log log log log log log log");
//        Mockito.when(supervisor.tailProcessStdoutLog(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(log);
//
//        this.mockMvc.perform(get("/api/tail/{name}?offset=500&length=1000", "program"))
//            .andExpect(status().isOk())
//            .andDo(document("tail-program-log", pathParameters(
//                parameterWithName("name").description("The name of the program to tail the log from")
//            ), requestParameters(
//                parameterWithName("offset").description("Offset to read from"),
//                parameterWithName("length").description("Length of log to read")
//            )));
//    }
//
//    @Test
//    public void tailProgramErrorLog() throws Exception {
//        TailLog log = new TailLog();
//        log.setOverflow(true);
//        log.setOffset(500);
//        log.setLog("log log log log log log log log log");
//        Mockito.when(supervisor.tailProcessStderrLog(Mockito.anyString(), Mockito.anyInt(), Mockito.anyInt())).thenReturn(log);
//
//        this.mockMvc.perform(get("/api/tail/err/{name}?offset=500&length=1000", "program"))
//            .andExpect(status().isOk())
//            .andDo(document("tail-program-error-log", pathParameters(
//                parameterWithName("name").description("The name of the program to tail the log from")
//            ), requestParameters(
//                parameterWithName("offset").description("Offset to read from"),
//                parameterWithName("length").description("Length of log to read")
//            )));
//    }
//
//    @Test
//    public void clearLog() throws Exception {
//        Mockito.when(supervisor.clearProcessLogs(Mockito.anyString())).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/log/clear/{name}", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("clear-log", pathParameters(
//                parameterWithName("name").description("The name of the program of which to clear the log")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void clearAllLogs() throws Exception {
//        Mockito.when(supervisor.clearAllProcessLogs()).thenReturn(util.getProcessStatus());
//
//        this.mockMvc.perform(post("/api/log/clear/all").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("clear-all-logs", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//}
