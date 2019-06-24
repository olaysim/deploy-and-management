package dk.syslab.supv.client.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.syslab.supv.client.SupvApi;
import dk.syslab.supv.client.SupvClient;
import dk.syslab.supv.dto.Program;
import dk.syslab.supv.dto.Result;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
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
import java.util.List;

/**
 * This class contains methods to upload/update, list files and delete programs on the Supervisor instance
 */
public class ProcessApi {
    private SupvClient client;
    private ObjectMapper objectMapper;

    public ProcessApi(SupvClient client, ObjectMapper mapper) {
        this.client = client;
        this.objectMapper = mapper;
    }

    /**
     * Use this method to create / upload / update a program.<br />
     * The fields of the Program is explained in the REST documentation https://syslab-XX:9080/docs/index.html#create-program<br />
     * If creating a new program, 3 fields are required: token, name and command. Otherwise if updating, then only updated fields are required + token.<br />
     * A list of File's can be supplied to upload the files (if the files are structured in folders, please use the other method to supply a list of Paths to describe the directory structure relative to the program directory, not the root directory)
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program an instance of Program
     * @param files a list of File's
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result uploadProgram(String host, String node, Program program, List<File> files, String token) throws IOException, URISyntaxException {
        return uploadProgram(host, node, program, files, null, null, token);
    }

    /**
     * Use this method to create / upload / update a program.<br />
     * The fields of the Program is explained in the REST documentation https://syslab-XX:9080/docs/index.html#create-program<br />
     * If creating a new program, 3 fields are required: token, name and command. Otherwise if updating, then only updated fields are required + token.<br />
     * A list of File's can be supplied to upload the files<br />
     * A list of relative paths describes the directory structure of the files. The paths are relative to the program's directory, not the root directory of the system)<br />
     * A list of transformations of nodes and file transformations can be supplied. A transformation consists of the node name and then a mapping of filenames as key and what filename the file should be transformed to when saved. That is, you give the original filename of the file that was uploaded and then a new filename that the file will be stored with
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program an instance of Program
     * @param files a list of File's
     * @param paths a list of relative paths
     * @param transforms a list of file transformations
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result uploadProgram(String host, String node, Program program, List<File> files, List<String> paths, List<String> transforms, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_PROCESS)
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
        return client.post(uri, Result.class, entity, Arrays.asList(authoriz));
    }

    /**
     * Use this method to remove a program from the Supervisor instance.
     * The program will first be "stopped" and then deleted. As it may take a short time to stop the program, this method can be slow (it is blocking).
     * If the method fails, the program may be left in an unknown state, you should contact an admin to resolve the issue.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param token a JWT authentication token
     * @return a success status
     * @throws IOException
     * @throws URISyntaxException
     */
    public Result deleteProgram(String host, String node, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_PROCESS + "/" + program)
                .build();
        return client.delete(uri, Result.class, null, SupvApi.getAcceptHeaders(token));
    }

    /**
     * Use this method to get a list of files and folders for a program.<br />
     * The result is a list of all the files and folders, relative to the directory of where the files are stored, not from the root folder of the filesystem.
     * @param host the address of the Supervisor REST API server
     * @param node the name (not address) of the the controller node to interact with
     * @param program program is the name of the program
     * @param token a JWT authentication token
     * @return a list of files
     * @throws IOException
     * @throws URISyntaxException
     */
    public List<String> listProgramFiles(String host, String node, String program, String token) throws IOException, URISyntaxException {
        URI uri = new URIBuilder()
                .setScheme("https")
                .setHost(host)
                .setPath(SupvApi.PATH_API + node + SupvApi.PATH_PROCESS + "/" + program)
                .build();
        String[] res = client.post(uri, String[].class, null, SupvApi.getAcceptHeaders(token));
        return Arrays.asList(res);
    }
}
