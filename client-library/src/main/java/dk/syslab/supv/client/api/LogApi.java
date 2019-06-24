package dk.syslab.supv.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Log;
import dk.syslab.supv.dto.Result;
import dk.syslab.supv.dto.supervisor.ProcessStatus;
import dk.syslab.supv.dto.supervisor.TailLog;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains methods to query, tail and clear the logs
 */
public class LogApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public LogApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to read length bytes from the Supervisor instance log.<br />
     * It can either return the entire log, a number of characters from the tail of the log, or a slice of the log specified by the offset and length parameters as described in the supervisor documentation.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param offset offset to start reading from.
     * @param length length number of bytes to read from the log
     * @return a segment of the log as defined by offset and length
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getLog(String host, String node, int offset, int length) throws IOException, URISyntaxException {
        return getLog(host, node, null, offset, length, false);
    }

    /**
     * Use this method to read 'length' bytes from the program's stdout log starting at 'offset'
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @return a segment of the log as defined by offset and length
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getLog(String host, String node, String program, int offset, int length) throws IOException, URISyntaxException {
        return getLog(host, node, program, offset, length, false);
    }

    /**
     * Use this method to read 'length' bytes from the program's error log starting at 'offset'
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @return a segment of the log as defined by offset and length
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getErrorLog(String host, String node, String program, int offset, int length) throws IOException, URISyntaxException {
        return getLog(host, node, program, offset, length, true);
    }

    /**
     * Undocumented method for internal use
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @param errorlog whether to read the STDOUT log or the STDERR log
     * @return a segment of the log as defined by offset and length
     * @throws IOException
     * @throws URISyntaxException
     */
    public String getLog(String host, String node, String program, int offset, int length, boolean errorlog) throws IOException, URISyntaxException {
        String url = SupvApi.PATH_API + node + SupvApi.PATH_LOG + "/" + program;
        if (program == null) url = SupvApi.PATH_API + node + SupvApi.PATH_LOG;
        if (errorlog) url = SupvApi.PATH_API + node + SupvApi.PATH_LOGERROR + "/" + program;

        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("offset",  String.valueOf(offset))
                .setParameter("length", String.valueOf(length))
                .build();
        Log res = client.get(uri, Log.class, SupvApi.getAcceptHeaders());
        return res.getLog();
    }

    /**
     * This method provides a more efficient way to tail the (STDOUT) log. Requests 'length' bytes from the programs’s log, starting at 'offset'.
     * If the total log size is greater than (offset + length), the overflow flag is set and the (offset) is automatically increased to position the buffer at the end of the log.
     * If less than 'length' bytes are available, the maximum number of available bytes will be returned. (offset) returned is always the last offset in the log +1.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the process (or 'group:name')
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @return a TailLog object containing the log, new offset and overflow
     * @throws IOException
     * @throws URISyntaxException
     */
    public TailLog getTail(String host, String node, String program, int offset, int length) throws IOException, URISyntaxException {
        return getTail(host, node, program, offset, length, false);
    }
    /**
     * This method provides a more efficient way to tail the (STDERR) log. Requests 'length' bytes from the programs’s log, starting at 'offset'.
     * If the total log size is greater than (offset + length), the overflow flag is set and the (offset) is automatically increased to position the buffer at the end of the log.
     * If less than 'length' bytes are available, the maximum number of available bytes will be returned. (offset) returned is always the last offset in the log +1.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the process (or 'group:name')
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @return a TailLog object containing the log, new offset and overflow
     * @throws IOException
     * @throws URISyntaxException
     */
    public TailLog getTailError(String host, String node, String program, int offset, int length) throws IOException, URISyntaxException {
        return getTail(host, node, program, offset, length, true);
    }
    /**
     * Undocumented
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the process (or 'group:name')
     * @param offset offset to start reading from
     * @param length length number of bytes to read from the log
     * @param errorlog whether to read the STDOUT log or the STDERR log
     * @return a TailLog object containing the log, new offset and overflow
     * @throws IOException
     * @throws URISyntaxException
     */
    public TailLog getTail(String host, String node, String program, int offset, int length, boolean errorlog) throws IOException, URISyntaxException {
        if (program == null || program.isEmpty()) throw new IllegalArgumentException("No program given");
        String url = SupvApi.PATH_API + node + SupvApi.PATH_TAIL + "/" + program;
        if (errorlog) url = SupvApi.PATH_API + node + SupvApi.PATH_TAILERROR + "/" + program;

        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("offset",  String.valueOf(offset))
                .setParameter("length", String.valueOf(length))
                .build();
        return client.get(uri, TailLog.class, SupvApi.getAcceptHeaders());
    }

    /**
     * Use this method to clear the stdout and stderr logs for ALL programs.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token
     * @return a list of ProcessStatus
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessStatus> clearAllLogs(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_LOGCLEAR + "/all")
                .build();
        ProcessStatus[] res = client.post(uri, ProcessStatus[].class, null, SupvApi.getAcceptHeaders(token));
        return Arrays.asList(res);
    }

    /**
     * Use this method to clear the stdout and stderr logs for the program and reopen them
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the process (or 'group:name').
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result clearLog(String host, String node, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_LOGCLEAR + "/" + program)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }
}
