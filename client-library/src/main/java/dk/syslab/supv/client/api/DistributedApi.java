package dk.syslab.supv.client.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Program;
import dk.syslab.supv.dto.distributed.*;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicHeader;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class contains the distributed API, which can perform many of the method from the other API wrapper, but they are executed in parallel on multiple nodes
 */
@SuppressWarnings("Duplicates")
public class DistributedApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public DistributedApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    public NodeList getNodes(String host)throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES)
                .build();
        return client.get(uri, NodeList.class, SupvApi.getAcceptHeaders());
    }

    public Map<String, ResultStatus> startProgram(String host, List<String> nodes, String program, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgram(host, nodes, program, true, wait, token);
    }
    public Map<String, ResultStatus> stopProgram(String host, List<String> nodes, String program, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgram(host, nodes, program, false, wait, token);
    }
    private Map<String, ResultStatus> startStopProgram(String host, List<String> nodes, String program, boolean start, boolean wait, String token) throws IOException, URISyntaxException {
        String url = SupvApi.PATH_NODES_START + "/" + program;
        if (!start) url = SupvApi.PATH_NODES_STOP + "/" + program;
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("wait", wait ? "1" : "0")
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultProcessStatus> startProgramGroup(String host, List<String> nodes, String group, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgramGroup(host, nodes, group, true, wait, token);
    }
    public Map<String, ResultProcessStatus> stopProgramGroup(String host, List<String> nodes, String group, boolean wait, String token) throws IOException, URISyntaxException {
        return startStopProgramGroup(host, nodes, group, false, wait, token);
    }
    private Map<String, ResultProcessStatus> startStopProgramGroup(String host, List<String> nodes, String group, boolean start, boolean wait, String token) throws IOException, URISyntaxException {
        String url = SupvApi.PATH_NODES_STARTGRP + "/" + group;
        if (!start) url = SupvApi.PATH_NODES_STOPGRP + "/" + group;
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("wait", wait ? "1" : "0")
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultProcessStatus>> typeRef = new TypeReference<HashMap<String, ResultProcessStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultProcessInfo> getProcessInfo(String host, List<String> nodes, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_INFO + "/" + program)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultProcessInfo>> typeRef = new TypeReference<HashMap<String, ResultProcessInfo>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> uploadProgram(String host, List<String> nodes, Program program, List<File> files, String token) throws IOException, URISyntaxException {
        return uploadProgram(host, nodes, program, files, null, null, token);
    }
    public Map<String, ResultStatus> uploadProgram(String host, List<String> nodes, Program program, List<File> files, List<String> paths, List<String> transforms, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_PROCESS)
                .build();

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.setCharset(Charset.defaultCharset());
        if (files != null) {
            for (File file : files) {
                ContentType contentType;
                try {
                    contentType = ContentType.parse(Files.probeContentType(file.toPath()));
                } catch (Exception ex) {
                    contentType = ContentType.DEFAULT_BINARY;
                }
                builder.addBinaryBody("files", file, contentType, file.getName());
            }
        }
        if (paths != null) {
            for (String path : paths) {
                builder.addTextBody("paths", path);
            }
        }
        if (transforms != null) {
            for (String transform : transforms) {
                builder.addTextBody("transforms", transform);
            }
        }
        for (String node : nodes) {
            builder.addTextBody("nodes", node);
        }

        if (program.getName() != null) builder.addTextBody("name", program.getName(), ContentType.DEFAULT_BINARY);
        if (program.getCommand() != null) builder.addTextBody("command", program.getCommand(), ContentType.DEFAULT_BINARY);
        if (program.getPriority() != null) builder.addTextBody("priority", String.valueOf(program.getPriority()), ContentType.DEFAULT_BINARY);
        if (program.getAutostart() != null) builder.addTextBody("autostart", String.valueOf(program.getAutostart()), ContentType.DEFAULT_BINARY);
        if (program.getAutorestart() != null) builder.addTextBody("autorestart", program.getAutorestart(), ContentType.DEFAULT_BINARY);
        if (program.getStartsecs() != null) builder.addTextBody("startsecs", String.valueOf(program.getStartsecs()), ContentType.DEFAULT_BINARY);
        if (program.getStartretries() != null) builder.addTextBody("startretries", String.valueOf(program.getStartretries()), ContentType.DEFAULT_BINARY);
        if (program.getExitcodes() != null) builder.addTextBody("exitcodes", program.getExitcodes(), ContentType.DEFAULT_BINARY);
        if (program.getStopwaitsecs() != null) builder.addTextBody("stopwaitsecs", String.valueOf(program.getStopwaitsecs()), ContentType.DEFAULT_BINARY);
        if (program.getEnvironment() != null) builder.addTextBody("environment", program.getEnvironment(), ContentType.DEFAULT_BINARY);

        HttpEntity entity = builder.build();
        Header authoriz = new BasicHeader("Authorization", "Bearer " + token);
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, Arrays.asList(authoriz));
    }

    public Map<String, ResultStatus> deleteProgram(String host, List<String> nodes, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_PROCESS + "/" + program)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.deleteMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> signalProgram(String host, List<String> nodes, String program, String signal, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_SIGNAL + "/" + program + "/" + signal)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> sendMessage(String host, List<String> nodes, String program, String message, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_SEND + "/" + program)
                .build();
        SendNodesData sendNodesData = new SendNodesData();
        sendNodesData.setData(message);
        sendNodesData.setNodes(nodes);
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(sendNodesData));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> sendCommEvent(String host, List<String> nodes, String type, String message, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_SENDCOMM)
                .build();
        SendNodesData sendNodesData = new SendNodesData();
        sendNodesData.setType(type);
        sendNodesData.setData(message);
        sendNodesData.setNodes(nodes);
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(sendNodesData));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> restart(String host, List<String> nodes, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_RESTART)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> clearLog(String host, List<String> nodes, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_CLEAR + "/" + program)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultStatus> update(String host, List<String> nodes, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_NODES_UPDATE)
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultStatus>> typeRef = new TypeReference<HashMap<String, ResultStatus>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }

    public Map<String, ResultTailLog> getTail(String host, List<String> nodes, String program, int offset, int length, String token) throws IOException, URISyntaxException {
        return getTail(host, nodes, program, offset, length, false, token);
    }
    public Map<String, ResultTailLog> getTailError(String host, List<String> nodes, String program, int offset, int length, String token) throws IOException, URISyntaxException {
        return getTail(host, nodes, program, offset, length, true, token);
    }
    public Map<String, ResultTailLog> getTail(String host, List<String> nodes, String program, int offset, int length, boolean errorlog, String token) throws IOException, URISyntaxException {
        if (program == null || program.isEmpty()) throw new IllegalArgumentException("No program given");
        String url = SupvApi.PATH_NODES_TAIL + "/" + program;
        if (errorlog) url = SupvApi.PATH_NODES_TAILERROR + "/" + program;

        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(url)
                .setParameter("offset",  String.valueOf(offset))
                .setParameter("length", String.valueOf(length))
                .build();
        StringEntity entity = new StringEntity(objectMapper.writeValueAsString(new RequestNodes(nodes)));
        TypeReference<HashMap<String, ResultTailLog>> typeRef = new TypeReference<HashMap<String, ResultTailLog>>() {};
        return client.postMap(uri, typeRef, entity, SupvApi.getAcceptHeaders(token));
    }
}
