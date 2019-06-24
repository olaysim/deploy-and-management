package dk.syslab.supv.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Result;
import dk.syslab.supv.dto.distributed.ReloadMap;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains administrative methods to restart and stop the supervisor instance and to clear the main log (requires admin privileges)
 */
public class AdminApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public AdminApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to stop the running Supervisor instance. <br />
     * Please be aware that the REST interface runs as an instance in Supervisor, and stopping the instance will kill the REST interface. Now you know, proceed with knowledge.<br />
     * A token with ADMINISTRATIVE privileges is required.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token (with admin privileges)
     * @return a success status (if not shutdown instantly)
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result shutdown(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SHUTDOWN)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to restarts the Supervisor instance. Programs that are set to autostart will.. autostart.<br />
     * A token with ADMINISTRATIVE privileges is required.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token (with admin privileges)
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result restart(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_RESTART)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to clear the main log of the Supervisor instance.<br />
     * A token with ADMINISTRATIVE privileges is required.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token (with admin privileges)
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result clearlog(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_CLEARLOG)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to reload the configuration.<br />
     * A token with ADMINISTRATIVE privileges is required.<br />
     * The result contains three arrays containing names of process groups:<br />
     * - added gives the process groups that have been added<br />
     * - changed gives the process groups whose contents have changed<br />
     * - removed gives the process groups that are no longer in the configuration
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token
     * @return a Map containing three lists of programs, if any changed status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Map<String, List<String>> reload(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_RELOAD)
                .build();
        ReloadMap res = client.post(uri, ReloadMap.class, null, SupvApi.getAcceptHeaders(token));
        Map<String, List<String>> map = new HashMap<>();
        List<String> added = res.getAdded();
        List<String> changed = res.getChanged();
        List<String> removed = res.getRemoved();
        map.put("added", added);
        map.put("changed", changed);
        map.put("removed", removed);
        return map;
    }
}
