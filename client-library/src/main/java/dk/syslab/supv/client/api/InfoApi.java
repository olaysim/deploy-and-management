package dk.syslab.supv.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.supervisor.ProcessInfo;
import dk.syslab.supv.dto.supervisor.SupervisorInfo;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains methods to get information about the supervisor and program instances
 */
public class InfoApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public InfoApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to get information abut the running Supervisor instance.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @return a SupervisorInfo object
     * @throws IOException
     * @throws URISyntaxException
     */
    public SupervisorInfo getSupervisorInfo(String host, String node) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_INFO)
                .build();
        SupervisorInfo res = client.get(uri, SupervisorInfo.class, SupvApi.getAcceptHeaders());
        return res;
    }

    /**
     * Use this method to get process-information about the programs running in the Supervisor instance
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program that you want information about
     * @return a ProcessInfo object
     * @throws IOException
     * @throws URISyntaxException
     */
    public ProcessInfo getProcessInfo(String host, String node, String program) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_INFO + "/" + program)
                .build();
        ProcessInfo res = client.get(uri, ProcessInfo.class, SupvApi.getAcceptHeaders());
        return res;
    }

    /**
     * Use this method to get a list of process-information. The list contains information about all the programs in the Supervisor instance
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @return a list containing information about all the programs in the Supervisor instance
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessInfo> getAllProcessInfo(String host, String node) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_INFO + "/all")
                .build();
        ProcessInfo[] res = client.get(uri, ProcessInfo[].class, SupvApi.getAcceptHeaders());
        return Arrays.asList(res);
    }
}
