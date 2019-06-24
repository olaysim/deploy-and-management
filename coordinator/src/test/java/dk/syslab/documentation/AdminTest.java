//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.AdminController;
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
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({AdminController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class AdminTest {
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
//        Mockito.when(validator.isAdmin(Mockito.any(Claims.class))).thenReturn(true);
//    }
//
//    @Test
//    public void stopSupervisor() throws Exception {
//        Mockito.when(supervisor.shutdown()).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/admin/shutdown").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("stop-supervisor", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void restartSupervisor() throws Exception {
//        Mockito.when(supervisor.restart()).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/admin/restart").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("restart-supervisor", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void clearMainLog() throws Exception {
//        Mockito.when(supervisor.clearLog()).thenReturn(true);
//
//        this.mockMvc.perform(post("/api/admin/clearlog").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("clear-main-log", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void reload() throws Exception {
//        Map<String, List<String>> res = new HashMap<>();
//        List<String> changed = new ArrayList<>();
//        changed.add("program1");
//        res.put("added", new ArrayList<>());
//        res.put("changed", changed);
//        res.put("removed", new ArrayList<>());
//        Mockito.when(supervisor.reloadConfig()).thenReturn(res);
//
//        this.mockMvc.perform(post("/api/admin/reload").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andDo(document("reload", requestHeaders(
//                    headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//                )));
//    }
//}
