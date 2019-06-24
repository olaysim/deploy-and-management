//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.ldap.LdapService;
//import dk.syslab.supv.ldap.LdapUser;
//import dk.syslab.supv.web.JwtService;
//import dk.syslab.supv.web.api.AuthController;
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
//import java.util.HashMap;
//import java.util.Map;
//
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({AuthController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class AuthTest {
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
//    LdapService ldapService;
//
//    @Autowired
//    JwtService jwtService;
//
//    @Before
//    public void setUp() {
//        this.mockMvc = util.setUp(context, restDocumentation);
//        Mockito.when(jwtService.getSigningKey()).thenReturn(util.getSigningKey());
//    }
//
//    @Test
//    public void Authenticate() throws Exception {
//        Map<String, Object> body = new HashMap<>();
//        body.put("username", "<username>");
//        body.put("password", "<password>");
//
//        LdapUser cu = Mockito.mock(LdapUser.class);
//        Mockito.when(cu.getUsername()).thenReturn("username");
//        Mockito.when(cu.hasRole(Mockito.anyString())).thenReturn(true);
//        Mockito.when(cu.getDisplayName()).thenReturn("Display Name");
//        Mockito.when(cu.getMail()).thenReturn("user@email.com");
//
//        Mockito.when(ldapService.authenticate("<username>", "<password>".toCharArray())).thenReturn(true);
//        Mockito.when(ldapService.getUser("<username>")).thenReturn(cu);
//
//        this.mockMvc.perform(post("/api/auth").contentType(MediaType.APPLICATION_JSON)
//            .content(mapper.writeValueAsString(body)))
//            .andExpect(status().isOk())
//            .andDo(document("authenticate", requestFields(
//                fieldWithPath("username").description("The user's username"),
//                fieldWithPath("password").description("The user's password")
//            ), responseFields(
//                fieldWithPath("token").description("The authentication token to use with REST calls"),
//                fieldWithPath("name").description("The full name of the user (mostly used for web pages with login components)"),
//                fieldWithPath("email").description("The email of the user (mostly used for web pages with login components)"),
//                fieldWithPath("expiresAt").description("The time at which the token expires (mostly used for web pages with login components)")
//            )));
//
//    }
//
//}
