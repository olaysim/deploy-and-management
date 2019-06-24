//package dk.syslab;
//
//import dk.syslab.supv.broadcast.BroadcastService;
//import dk.syslab.supv.storage.CleanUpService;
//import dk.syslab.supv.ldap.LdapService;
//import dk.syslab.supv.rpc.model.statistics.StatisticsService;
//import dk.syslab.supv.storage.FileService;
//import dk.syslab.supv.web.JwtService;
//import dk.syslab.supv.web.Validator;
//import dk.syslab.supv.rpc.model.xmlrpc.XmlRpcService;
//import org.mockito.Mockito;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.Primary;
//import org.springframework.context.annotation.Profile;
//
//@Profile("test")
//@Configuration
//public class AppConfigTest {
//
//    @Bean
//    @Primary
//    public StatisticsService statisticsService() {
//        return Mockito.mock(StatisticsService.class);
//    }
//
//    @Bean
//    @Primary
//    public CleanUpService cleanUpService() {
//        return Mockito.mock(CleanUpService.class);
//    }
//
//    @Bean
//    @Primary
//    public JwtService jwtServiceTest() {
//        return Mockito.mock(JwtService.class);
//    }
//
//    @Bean
//    @Primary
//    public LdapService ldapServiceTest() {
//        return Mockito.mock(LdapService.class);
//    }
//
//    @Bean
//    @Primary
//    public XmlRpcService xmlRpcService() throws Exception {
////        Environment env = Mockito.mock(Environment.class);
////        Mockito.when(env.getRequiredProperty("xmlrpc.username")).thenReturn("mocked test string");
////        Mockito.when(env.getRequiredProperty("xmlrpc.password")).thenReturn("mocked test string");
////        Mockito.when(env.getRequiredProperty("xmlrpc.port")).thenReturn("1234");
////        Mockito.when(env.getRequiredProperty("xmlrpc.file")).thenReturn("/");
//        XmlRpcService service = Mockito.mock(XmlRpcService.class);
//        Mockito.doNothing().when(service).updateState();
////        PowerMockito.doNothing().when(service.updateState())
////        PowerMockito.doNothing().when(spy, "updateState");
////        PowerMockito.doNothing().when(spy, MemberMatcher.method(XmlRpcService.class, "updateSupervisorInfo")).withNoArguments();
////        PowerMockito.doNothing().when(spy, MemberMatcher.method(XmlRpcService.class, "updateSupervisorInfo")).withNoArguments();
////        PowerMockito.doNothing().when(XmlRpcService.class, MemberMatcher.method(XmlRpcService.class, "updateSupervisorInfo"));
//        return service;
//    }
//
//    @Bean
//    @Primary
//    public Validator validateToken() {
//        return Mockito.mock(Validator.class);
//    }
//
//    @Bean
//    @Primary
//    public BroadcastService broadcastService() {
//        BroadcastService service = Mockito.mock(BroadcastService.class);
//        Mockito.doNothing().when(service).removeOldNodes();
//        return service;
//    }
//
//    @Bean
//    @Primary
//    public FileService fileService() {
//        return Mockito.mock(FileService.class);
//    }
//}
