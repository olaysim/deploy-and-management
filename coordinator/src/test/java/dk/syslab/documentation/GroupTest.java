//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.storage.FileService;
//import dk.syslab.supv.storage.Groups;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.GroupController;
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
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.*;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({GroupController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class GroupTest {
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
//    Validator validator;
//
//    @Autowired
//    FileService fileService;
//
//    @Before
//    public void setUp() throws IOException {
//        this.mockMvc = util.setUp(context, restDocumentation);
//
//        Mockito.when(validator.validate(Mockito.any(String.class), Mockito.any(HttpServletResponse.class))).thenReturn(new DefaultClaims());
//        Mockito.when(fileService.validName(Mockito.anyString())).thenReturn(true);
//        Mockito.when(validator.isAdmin(Mockito.any(Claims.class))).thenReturn(false);
//    }
//
//    @Test
//    public void addProcessToGroup() throws Exception {
//        Mockito.doNothing().when(fileService).addProgramToGroup(Mockito.anyString(), Mockito.anyString(), Mockito.anyInt());
//
//        this.mockMvc.perform(post("/api/group/{group}/{name}?priority=1", "groupX", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("add-process-to-group", pathParameters(
//                parameterWithName("group").description("The name of the group to add the program to"),
//                parameterWithName("name").description("The name of the program")
//            ), requestParameters(
//                parameterWithName("priority").optional().description("(Optional) A priority number analogous to a `program` priority value assigned to the group. (default 999)")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void deleteProcessFromGroup() throws Exception {
//        Mockito.doNothing().when(fileService).removeProgramFromGroup(Mockito.anyString(), Mockito.anyString());
//
//        this.mockMvc.perform(delete("/api/group/{group}/{name}", "groupX", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("delete-process-from-group", pathParameters(
//                parameterWithName("group").description("The name of the group to add the program to"),
//                parameterWithName("name").description("The name of the program")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void listGroups() throws Exception {
//        Groups grps = new Groups();
//        grps.add("group1", "program1");
//        grps.add("group1", "program2");
//        grps.add("group2", "program3");
//        Mockito.when(fileService.listGroups()).thenReturn(grps);
//
//        this.mockMvc.perform(get("/api/group"))
//            .andExpect(status().isOk());
//    }
//}
