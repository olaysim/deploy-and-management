package dk.syslab.supv.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Result;
import dk.syslab.supv.dto.SendData;
import dk.syslab.supv.dto.supervisor.ProcessStatus;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;

/**
 * This class contains methods to start, stop, signal, reload and send messages to programs
 */
public class CommandApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public CommandApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to start a program. 'wait' wait defines whether to block and wait for the program to start before the method returns or to return right away.
     * The default is to return immediately. If 'wait' is set to 0, then getProcessInfo() can be used to poll the status of the program. 'wait' is optional.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program name is the name of the program.
     * @param wait wait defines whether to block and wait for the program to start before the method returns or to return right away.
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result startProgram(String host, String node, String program, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgram(host, node, program, true, wait, token);
    }
    /**
     * Use this method to stop a program. 'wait' defines whether to block and wait for the program to stop before the method returns or to return right away.
     * The default is to return immediately. If 'wait' is set to 0, getProcessInfo() could be used to poll the status of the program. 'wait' is optional.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program name is the name of the program.
     * @param wait wait defines whether to block and wait for the program to stop before the method returns or to return right away
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result stopProgram(String host, String node, String program, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgram(host, node, program, false, wait, token);
    }
    private Result startStopProgram(String host, String node, String program, boolean start, boolean wait, String token) throws IOException, URISyntaxException {
        String url = SupvApi.PATH_API + node + SupvApi.PATH_START + "/" + program;
        if (!start) url = SupvApi.PATH_API + node + SupvApi.PATH_STOP + "/" + program;
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("wait", wait ? "1" : "0")
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use POST this method to start a program group. 'wait' defines whether to block and wait for the program group to start before the method returns or to return right away.
     * The default is to return immediately. 'wait' is optional.<br />
     * Programs can be added and removed from groups with addProgramToGroup() / deleteProgramFromGroup()
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param group group is the name of the program group
     * @param wait wait defines whether to block and wait for the program group to start before the method returns or to return right away
     * @param token a JWT authentication token
     * @return a list of ProcessStatus
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessStatus> startProgramGroup(String host, String node, String group, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgramGroup(host, node, group, true, wait, token);
    }
    /**
     * Use this method to stop a program group. 'wait' defines whether to block and wait for the program group to stop before the method returns or to return right away.
     * The default is to return immediately. 'wait' is optional.<br />
     * Programs can be added and removed from groups with addProgramToGroup() / deleteProgramFromGroup()
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param group group is the name of the program group
     * @param wait wait defines whether to block and wait for the program group to stop before the method returns or to return right away
     * @param token a JWT authentication token
     * @return a list of ProcessStatus
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessStatus> stopProgramGroup(String host, String node, String group, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgramGroup(host, node, group, false, wait, token);
    }
    private List<ProcessStatus> startStopProgramGroup(String host, String node, String group, boolean start, boolean wait, String token) throws IOException, URISyntaxException {
        String url = SupvApi.PATH_API + node + SupvApi.PATH_STARTGRP + "/" + group;
        if (!start) url = SupvApi.PATH_API + node + SupvApi.PATH_STOPGRP + "/" + group;
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("wait", wait ? "1" : "0")
                .build();
        ProcessStatus[] res = client.post(uri, ProcessStatus[].class, null, SupvApi.getAcceptHeaders(token));
        return Arrays.asList(res);
    }

    /**
     * Use this method to send an arbitrary UNIX signal to the process named by 'name'
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program name is the name of the program
     * @param signal signal is the signal to send and can be a name e.g. 'HUP' or number '1'
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result signalProgram(String host, String node, String program, String signal, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SIGNAL + "/" + program + "/" + signal)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to send an arbitrary UNIX signal to all the programs in the program group named by 'name'.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param group group is the name of the program group
     * @param signal signal is the signal to send and can be a name e.g. 'HUP' or number '1'
     * @param token a JWT authentication token
     * @return a list of ProcessStatus
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessStatus> signalProgramGroup(String host, String node, String group, String signal, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SIGNALGRP + "/" + group + "/" + signal)
                .build();
        ProcessStatus[] res = client.post(uri, ProcessStatus[].class, null, SupvApi.getAcceptHeaders(token));
        return Arrays.asList(res);
    }

    /**
     * Use this method to send an arbitrary UNIX signal to ALL the programs on the Supervisor instance
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param signal signal is the signal to send and can be a name e.g. 'HUP' or number '1'
     * @param token a JWT authentication token
     * @return a list of ProcessStatus
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<ProcessStatus> signalAllPrograms(String host, String node, String signal, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SIGNALALL + "/" + signal)
                .build();
        ProcessStatus[] res = client.post(uri, ProcessStatus[].class, null, SupvApi.getAcceptHeaders(token));
        return Arrays.asList(res);
    }

    /**
     * Use this method to update the configuration (reloads config and restarts programs as needed)
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result update(String host, String node, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_UPDATE)
                .build();
        return client.post(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to send a string of chars to the STDIN of the program name 'name'
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param message a string of characters to send to STDIN of the program
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result sendMessage(String host, String node, String program, String message, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SEND + "/" + program)
                .build();
        SendData sendData = new SendData();
        sendData.setData(message);
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(sendData));
        return client.post(uri, Result.class, entity, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to send an event that will be received by event listener subprocesses subscribing to the RemoteCommunicationEvent.
     * The Supervisor instance has an internal webserver that programs can hook into and listen to events.
     * Excatly how this is done is unknown, but you are encouraged to browse the Supervisor documentation.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param type the type of the message
     * @param message the message
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result sendCommEvent(String host, String node, String type, String message, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_SENDCOMM)
                .build();
        SendData sendData = new SendData();
        sendData.setType(type);
        sendData.setData(message);
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(sendData));
        return client.post(uri, Result.class, entity, SupvApi.getAcceptHeaders(token));
    }
}
