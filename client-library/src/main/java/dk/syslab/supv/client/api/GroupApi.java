package dk.syslab.supv.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Result;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class contains methods to list groups and their programs and methods to add and remove programs from groups
 */
public class GroupApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public GroupApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to get a list of groups. Each group contains a list programs that are currently in that group.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @return a Map of groups with a list of programs in that that group
     * @throws IOException
     * @throws URISyntaxException
     */
    public Map<String, ArrayList<String>> getGroups(String host, String node) throws IOException, URISyntaxException {
            URI uri = new URIBuilder()
                    .setScheme("https")
                    .setHost(host)
                    .setPath(SupvApi.PATH_API + node + SupvApi.PATH_GROUP)
                    .build();
            TypeReference<HashMap<String, ArrayList<String>>> typeRef = new TypeReference<HashMap<String, ArrayList<String>>>() {};
            return client.getMap(uri, typeRef, SupvApi.getAcceptHeaders());
    }

    /**
     * Use this method to add a program to a group. 'name' is the name of the program and 'group' is the name of the group.<br />
     * You will need to call reload() to have the changes take effect.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program name is the name of the program
     * @param group group is the name of the program group
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result addProgramToGroup(String host, String node, String program, String group, String token) throws IOException, URISyntaxException {
        return addProgramToGroup(host, node, program, group, 999, token);
    }
    public Result addProgramToGroup(String host, String node, String program, String group, int priority, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_GROUP + "/" + group + "/" + program)
                .setParameter("priority", String.valueOf(priority))
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to remove a program to a group. 'name' is the name of the program and 'group' is the name of the group.<br />
     * You will need to call reload() to have the changes take effect.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program name is the name of the program
     * @param group group is the name of the program group
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result deleteProgramFromGroup(String host, String node, String program, String group, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_GROUP + "/" + group + "/" + program)
                .build();
        return client.delete(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }
}
