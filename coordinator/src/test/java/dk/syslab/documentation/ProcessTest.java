//package dk.syslab.documentation;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import dk.syslab.AppConfigTest;
//import dk.syslab.supv.storage.FileService;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.web.api.ProcessController;
//import dk.syslab.supv.web.api.model.Program;
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
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.context.WebApplicationContext;
//
//import javax.servlet.http.HttpServletResponse;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.springframework.restdocs.headers.HeaderDocumentation.headerWithName;
//import static org.springframework.restdocs.headers.HeaderDocumentation.requestHeaders;
//import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
//import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
//import static org.springframework.restdocs.payload.PayloadDocumentation.*;
//import static org.springframework.restdocs.request.RequestDocumentation.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@ActiveProfiles("test")
//@RunWith(SpringRunner.class)
//@WebMvcTest({ProcessController.class, AppConfigTest.class})
//@ComponentScan("dk.syslab")
//@AutoConfigureWebClient
//public class ProcessTest {
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
//    @Autowired
//    FileService fileService;
//
//    @Before
//    public void setUp() throws IOException {
//        this.mockMvc = util.setUp(context, restDocumentation);
//
//        Mockito.when(validator.validate(Mockito.any(String.class), Mockito.any(HttpServletResponse.class))).thenReturn(new DefaultClaims());
//        Mockito.when(validator.isAdmin(Mockito.any(Claims.class))).thenReturn(true);
//        Mockito.when(validator.validateName(Mockito.anyString())).thenReturn(true);
//        Mockito.when(validator.validateDirectory(Mockito.anyString())).thenReturn(true);
//        Mockito.when(validator.validateFilename(Mockito.anyString())).thenReturn(true);
//    }
//
//    @Test
//    public void uploadProgram() throws Exception {
//        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
//        body.add("name", "<name>");
//        body.add("command", "<command>");
//
//        Mockito.when(fileService.programExists(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
//        Mockito.when(fileService.validName(Mockito.anyString())).thenReturn(true);
//        Mockito.doNothing().when(fileService).storeProgram(Mockito.anyString(), Mockito.any(Program.class));
//        Mockito.doNothing().when(fileService).storeConfiguration(Mockito.anyString(), Mockito.any(Program.class));
//
//        this.mockMvc.perform(post("/api/process").header("Authorization", "Bearer <auth token>")
//            .params(body))
//            .andExpect(status().isOk())
//            .andDo(document("upload-program", requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), requestParameters(
//                parameterWithName("name").description("The name of the program. Only use A-Z and 0-9 (don't use brackets, underscores etc)"),
//                parameterWithName("command").description("The command to run! (relative path)"),
//                parameterWithName("priority").optional().description("(Optional) The relative priority of the program in the start and shutdown ordering. Lower priorities indicate programs that start first and shut down last at startup and when aggregate commands are used in various clients (e.g. “start all”/”stop all”). Higher priorities indicate programs that start last and shut down first. (default 999)"),
//                parameterWithName("autostart").optional().description("(Optional) If true, this program will start automatically when supervisor is started. (boolean, default true)"),
//                parameterWithName("autorestart").optional().description("(Optional) Specifies if supervisor should automatically restart a process if it exits when it is in the `RUNNING` state. May be one of `false`, `unexpected`, or `true`. If `false`, the process will not be autorestarted. If `unexpected`, the process will be restarted when the program exits with an exit code that is not one of the exit codes associated with this process’ configuration. If `true`, the process will be unconditionally restarted when it exits, without regard to its exit code. (default \"unexpected\")"),
//                parameterWithName("startsecs").optional().description("(Optional) The total number of seconds which the program needs to stay running after a startup to consider the start successful (moving the process from the `STARTING` state to the `RUNNING` state). Set to 0 to indicate that the program needn't stay running for any particular amount of time. (default 3)"),
//                parameterWithName("startretries").optional().description("(Optional) The number of serial failure attempts that supervisor will allow when attempting to start the program before giving up and putting the process into an FATAL state.  (default 3)"),
//                parameterWithName("exitcodes").optional().description("(Optional) The list of “expected” exit codes for this program used with `autorestart`. If the `autorestart` parameter is set to `unexpected`, and the process exits in any other way than as a result of a supervisor stop request, supervisor will restart the process if it exits with an exit code that is not defined in this list. (default is 0,2) A Java program is different, e.g. A Spring Boot application uses 143."),
//                parameterWithName("stopwaitsecs").optional().description("(Optional) The number of seconds to wait for the OS to return a SIGCHLD to supervisor after the program has been sent a stopsignal. If this number of seconds elapses before supervisor receives a SIGCHLD from the process, supervisor will attempt to kill it with a final SIGKILL."),
//                parameterWithName("environment").optional().description("(Optional) A list of key/value pairs in the form KEY=\"val\",KEY2=\"val2\" that will be placed in the child process’ environment. The environment string may contain Python string expressions that will be evaluated against a dictionary containing `group_name`, `host_node_name`, `program_name`, and `here` (the directory of the supervisor config file). Values containing non-alphanumeric characters should be quoted (e.g. KEY=\"val:123\",KEY2=\"val,456\"). Otherwise, quoting the values is optional but recommended. Note that the subprocess will inherit the environment variables of the shell used to start “supervisor” except for the ones overridden here"),
//                parameterWithName("files").optional().description("(Optional) A list of MultiPart files"),
//                parameterWithName("paths").optional().description("(Optional) A map (delimited string) of relative paths, consisting of the filenames as key and their relative paths as value. If this map is supplied the files will be copied to their relative paths otherwise all files will be put in the same folder."),
//                parameterWithName("transforms").optional().description("(Optional) A map of maps (delimited string) of nodes and file transformations. A transformation consists of the node name and then a mapping of filenames as key and what filename the file should be transformed to when saved. That is, you give the original filename of the file that was uploaded and then a new filename that the file will be stored with. The purpose is to take e.g. config files stored as syslab-01.config.xml and rename them to config.xml on each node respectively. Additionally a relative path can be given for the new filename in the relative paths list, if the file should be placed in some structure as well.")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void deleteProgram() throws Exception {
//        Mockito.when(supervisor.stopProcess(Mockito.anyString(), Mockito.anyBoolean())).thenReturn(true);
//        Mockito.doNothing().when(fileService).deleteProgram(Mockito.anyString(), Mockito.anyString());
//
//        this.mockMvc.perform(delete("/api/process/{name}", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("delete-program", pathParameters(
//                parameterWithName("name").description("The name of the program to delete")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            ), responseFields(
//                fieldWithPath("success").description("Describes whether the operation was a success"),
//                fieldWithPath("description").description("May contain a description of the error if not success")
//            )));
//    }
//
//    @Test
//    public void listProgramFiles() throws Exception {
//        List<String> values = new ArrayList<>();
//        values.add("/root-file 1.txt");
//        values.add("/root-file 2.txt");
//        values.add("/somefolder/folder-file 3.txt");
//        Mockito.when(fileService.listProgramFiles(Mockito.anyString(), Mockito.anyString())).thenReturn(values);
//
//        this.mockMvc.perform(post("/api/process/{name}", "program").header("Authorization", "Bearer <auth token>").contentType(MediaType.APPLICATION_JSON))
//            .andExpect(status().isOk())
//            .andDo(document("list-program-files", pathParameters(
//                parameterWithName("name").description("The name of the program to list files for")
//            ), requestHeaders(
//                headerWithName("Authorization").description("The authentication token using the Bearer Scheme")
//            )));
//    }
//}
