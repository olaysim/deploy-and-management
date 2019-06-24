package dk.syslab.supv.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.api.*;
import dk.syslab.supv.dto.AuthRequest;
import dk.syslab.supv.dto.AuthResponse;
import dk.syslab.supv.dto.TokenRequest;
import dk.syslab.supv.dto.TokenResponse;
import org.apache.http.Header;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * This class is a Java wrapper around the API exposed by the Supervisor REST API.<br />
 * The purpose is to make it extraordinarily easy to use the REST API :-) <br/>
 * This class consists of several subclasses e.g. InfoApi, which contains API methods grouped in categories. The methods are grouped both to make it easier for the user to find the correct method as well as keeping the code clean.<br />
 * <br />
 * info() contains methods to get information about the supervisor and program instances<br />
 * log() contains methods to query the logs<br />
 * group() contains methods to list groups and their programs and methods to add and remove programs from groups<br />
 * program() contains methods to  upload/update, list files and delete programs on the Supervisor instance<br />
 * command() contains methods to start, stop, signal, reload and send messages to programs<br />
 * admin() contains administrative methods to restart and stop the supervisor instance (requires admin privileges)<br />
 * nodes() contains the distributed API, which can perform many of the previously mentioned actions but they are executed in parallel on multiple nodes
 */
public class SupvApi {
    public final static String PATH_API             = "/api/";
    public final static String PATH_AUTHENTICATE    = "/auth";
    public final static String PATH_INFO            = "/info";
    public final static String PATH_LOG             = "/log";
    public final static String PATH_LOGERROR        = "/log/err";
    public final static String PATH_LOGCLEAR        = "/log/clear";
    public final static String PATH_TAIL            = "/tail";
    public final static String PATH_TAILERROR       = "/tail/err";
    public final static String PATH_GROUP           = "/group";
    public final static String PATH_PROCESS         = "/process";
    public final static String PATH_START           = "/start";
    public final static String PATH_STARTGRP        = "/start/group";
    public final static String PATH_STOP            = "/stop";
    public final static String PATH_STOPGRP         = "/stop/group";
    public final static String PATH_SIGNAL          = "/signal";
    public final static String PATH_SIGNALALL       = "/signal/all";
    public final static String PATH_SIGNALGRP       = "/signal/group";
    public final static String PATH_UPDATE          = "/update";
    public final static String PATH_SEND            = "/send";
    public final static String PATH_SENDCOMM        = "/sendcomm";
    public final static String PATH_TOKEN           = "/token";
    public final static String PATH_SHUTDOWN        = "/admin/shutdown";
    public final static String PATH_RESTART         = "/admin/restart";
    public final static String PATH_CLEARLOG        = "/admin/clearlog";
    public final static String PATH_RELOAD          = "/admin/reload";

    public final static String PATH_NODES           = "/api/nodes";
    public final static String PATH_NODES_START     = "/api/nodes/start";
    public final static String PATH_NODES_STARTGRP  = "/api/nodes/start/group";
    public final static String PATH_NODES_STOP      = "/api/nodes/stop";
    public final static String PATH_NODES_STOPGRP   = "/api/nodes/stop/group";
    public final static String PATH_NODES_INFO      = "/api/nodes/info";
    public final static String PATH_NODES_PROCESS   = "/api/nodes/process";
    public final static String PATH_NODES_SIGNAL    = "/api/nodes/signal";
    public final static String PATH_NODES_SEND      = "/api/nodes/send";
    public final static String PATH_NODES_SENDCOMM  = "/api/nodes/sendcomm";
    public final static String PATH_NODES_RESTART   = "/api/nodes/restart";
    public final static String PATH_NODES_CLEAR     = "/api/nodes/clear";
    public final static String PATH_NODES_UPDATE    = "/api/nodes/update";
    public final static String PATH_NODES_TAIL      = "/api/nodes/tail";
    public final static String PATH_NODES_TAILERROR = "/api/nodes/tail/err";

    private SupvClient client;
    private ObjectMapper objectMapper;

    private InfoApi infoApi;
    private LogApi logApi;
    private GroupApi groupApi;
    private ProcessApi processApi;
    private CommandApi commandApi;
    private AdminApi adminApi;
    private DistributedApi distributedApi;

    public SupvApi(SupvClient client) {
        this.client = client;
        this.objectMapper = new ObjectMapper();

        infoApi = new InfoApi(client, objectMapper);
        logApi = new LogApi(client, objectMapper);
        groupApi = new GroupApi(client, objectMapper);
        processApi = new ProcessApi(client, objectMapper);
        commandApi = new CommandApi(client, objectMapper);
        adminApi = new AdminApi(client, objectMapper);
        distributedApi = new DistributedApi(client, objectMapper);
    }

    /**
     * Creates a set of headers that configures the HTTP call to use JSON.
     * This method is primarily meant for internal use.
     * @return a set of headers with a JSON configuration
     */
    public static List<Header> getAcceptHeaders() {
        Header accept = new BasicHeader("Accept", "application/json");
        Header content = new BasicHeader("Content-type", "application/json");
        return Arrays.asList(accept, content);
    }

    /**
     * Creates a set of headers that configures the HTTP call to use JSON.
     * Includes the Authorization Bearer token.
     * This method is primarily meant for internal use.
     * @param token Authorization token using the Bearer Scheme
     * @return a set of headers with a JSON configuration and the authorization token
     */
    public static List<Header> getAcceptHeaders(String token) {
        Header accept = new BasicHeader("Accept", "application/json");
        Header content = new BasicHeader("Content-type", "application/json");
        Header authoriz = new BasicHeader("Authorization", "Bearer " + token);
        return Arrays.asList(accept, content, authoriz);
    }

    /**
     * Use this method to authenticate against the Supervisor API.<br />
     * To authenticate, you need to call this method with your campusnet credentials (username/password) as the application uses the DTU AD and the SYSLAB LDAP to look up your user and to verify your privileges in SYSLAB. You will then receive a JWT token that you can use to authenticate against every other SYSLAB node (Supervisor instance) in the network.<br/>
     * The JWT token provides distributed and decentralized authentication, which means that you do not need to authenticate against every host, you can use this token for every host and only need to authenticate once. (The lifetime of this token is limited and if you need a token that is valid for longer you should look at the generate token method.)
     * @param host the address of the Supervisor REST API server
     * @param username Your username
     * @param password Your password
     * @return a JWT authentication token
     * @throws IOException
     * @throws URISyntaxException
     */
    public AuthResponse authenticate(String host, String username, String password) throws IOException, URISyntaxException {
        AuthRequest req = new AuthRequest(username, password);
        if (!req.isReady()) throw new IllegalArgumentException("Username or password was not supplied");
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(PATH_API + PATH_AUTHENTICATE)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(req));
        AuthResponse res = client.post(uri, AuthResponse.class, entity, getAcceptHeaders());
        return res;
    }

    /**
     * Use this method to generate an authentication token with a long validity, primarily for use with m2m communication.
     * To use thus method, a valid token must be supplied, which is used to generate a new token with a custom life period.<br />
     * Only an authentication token that already has admin privileges can be used to create a token with admin privileges.
     * @param host the address of the Supervisor REST API server
     * @param days days is how many days the token should be valid for and is optional, default is 30 days.
     * @param admin admin is whether the token should give admin privileges, it’s optional and default is false
     * @param token token is the authentication token that you got when logging in with /api/auth (but of course any valid token can be used).
     * @return a new JWT token with a long validity (as specified)
     * @throws IOException
     * @throws URISyntaxException
     */
    public TokenResponse generateToken(String host, int days, boolean admin, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(PATH_API + PATH_TOKEN)
                .build();
        TokenRequest req = new TokenRequest();
        req.setDays(days);
        req.setAdmin(admin);
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(req));
        return client.post(uri, TokenResponse.class, entity, SupvApi.getAcceptHeaders(token));
    }

    /**
     * contains methods to get information about the supervisor and program instances
     */
    public InfoApi info() {
        return infoApi;
    }

    /**
     * contains methods to query the logs
     */
    public LogApi log() {
        return logApi;
    }

    /**
     * contains methods to list groups and their programs and methods to add and remove programs from groups
     */
    public GroupApi group() {
        return groupApi;
    }

    /**
     * contains methods to  upload/update, list files and delete programs on the Supervisor instance
     */
    public ProcessApi program() {
        return processApi;
    }

    /**
     * contains methods to start, stop, signal, reload and send messages to programs
     */
    public CommandApi command() {
        return commandApi;
    }

    /**
     * contains administrative methods to restart and stop the supervisor instance (requires admin privileges)
     */
    public AdminApi admin() {
        return adminApi;
    }

    /**
     * contains the distributed API, which can perform many of the previously mentioned actions but they are executed in parallel on multiple nodes
     */
    public DistributedApi nodes() {
        return distributedApi;
    }
}
