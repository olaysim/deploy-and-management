//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.web.JwtService;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.TokenController;
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
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({TokenController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class TokenTest {
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
//    JwtService jwtService;
//
//    @Before
//    public void setUp() throws IOException {
//        this.mockMvc = util.setUp(context, restDocumentation);
//
//        Claims claims = new DefaultClaims();
//        claims.setSubject("<username>");
//        Mockito.when(validator.validate(Mockito.any(String.class), Mockito.any(HttpServletResponse.class))).thenReturn(claims);
//        Mockito.when(validator.isAdmin(Mockito.any(Claims.class))).thenReturn(false);
//        Mockito.when(jwtService.getSigningKey()).thenReturn(util.getSigningKey());
//    }
//
//    @Test
//    public void generateToken() throws Exception {
//        Map<String, Object> body = new HashMap<>();
//        body.put("days", "30");
//        body.put("admin", "false");
//
//        this.mockMvc.perform(post("/api/token").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON)
//            .content(mapper.writeValueAsString(body)))
//            .andExpect(status().isOk())
//            .andDo(document("generate-token", requestHeaders(
//                headerWithName("Authorization").description("This is the authentication token you received when you logged in. The generated token is tied to you and all actions and changes made with this token is performed with your username")
//            ), requestFields(
//                fieldWithPath("days").optional().description("(Optional) How many days the token should be valid"),
//                fieldWithPath("admin").optional().description("(Optional) Whether the token should have administrative privileges")
//            ), responseFields(
//                fieldWithPath("token").description("The newly generated token!")
//            )));
//    }
//}
