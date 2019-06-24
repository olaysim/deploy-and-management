package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.model.NodeList;
import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.DistributedRpcService;
import dk.syslab.supv.rpc.FileRpcService;
import dk.syslab.supv.rpc.model.FileUuidBytes;
import dk.syslab.supv.storage.FileService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.distributed.*;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.UUID;

@RestController
public class NodeController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    DistributedRpcService distributedRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @Autowired
    Validator validator;

    @Autowired
    FileService fileService;

    @Autowired
    FileRpcService fileRpcService;

    @RequestMapping(
        value = "/api/nodes",
        method = RequestMethod.GET
    )
    public NodeList listNodes() {
        return broadcastRpcService.getNodeList(broadcastRpcService.getDefaultNode());
    }

    @RequestMapping(
        value = "/api/nodes/start/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> startPrograms(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return distributedRpcService.startPrograms(token, nodes.getNodes(), name, true);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/stop/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> stopPrograms(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return distributedRpcService.stopPrograms(token, nodes.getNodes(), name, true);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/start/group/{name}",
        method = RequestMethod.POST
    )
    public SortedMap<String, ResultProcessStatus> startProgramGroups(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
                return null;
            }
            return distributedRpcService.startProgramGroups(token, nodes.getNodes(), name, true);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/stop/group/{name}",
        method = RequestMethod.POST
    )
    public SortedMap<String, ResultProcessStatus> stopProgramGroups(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
                return null;
            }
            return distributedRpcService.stopProgramGroups(token, nodes.getNodes(), name, true);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/info/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultProcessInfo> getProgramInfos(@RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        // no validation of token, this is essentially a GET method, but it is simply easier to use the same POST as with authenticated methods, for the client
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
            return null;
        }
        return distributedRpcService.getProcessInfo(nodes.getNodes(), name);
    }

    @RequestMapping(
        value = "/api/nodes/process",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> uploadPrograms(@RequestHeader(value = "Authorization", required = false) String token, @ModelAttribute ProgramNodes program, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (!validator.isAdmin(claims) && (!validator.validateBannedName(claims.getSubject()) || !validator.validateBannedName(program.getName()))) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name or username is invalid or not allowed!");
                return null;
            }
            if (!validator.validateName(program.getName())) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name contains invalid characters");
                return null;
            }
            String programUuid = UUID.randomUUID().toString();
            String target = distributedRpcService.selectHostFromNodeList(program.getNodes());
            target = "127.0.0.1";
            FineUploaderProgram fineProgram = fileService.saveProgramToTemporaryLocation(program, programUuid);
            List<FileUuidBytes> fileUuidBytes = fileService.generateFileUuidBytesList(fineProgram);
            fileRpcService.uploadFiles(target, token, fileUuidBytes);
            return distributedRpcService.uploadProgram(target, token, program.getNodes(), fineProgram);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/process/{name}",
        method = RequestMethod.DELETE
    )
    public Map<String, ResultStatus> deleteProcess(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No program names was supplied");
            return null;
        }
        if (!validator.validateName(name)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name contains invalid characters");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            distributedRpcService.stopPrograms(token, nodes.getNodes(), name, true);
            return distributedRpcService.deleteProgram(token, nodes.getNodes(), name);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/signal/{name}/{signal}",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> signalPrograms(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, @PathVariable String signal, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            if (signal == null || signal.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No signal type supplied");
                return null;
            }
            return distributedRpcService.signalProgram(token, nodes.getNodes(), name, signal);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/send/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> sendMessage(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) SendNodesData nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (nodes == null || !nodes.isDataReady()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed (either token or data is missing)");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return distributedRpcService.sendMessage(token, nodes.getNodes(), name, nodes.getData());
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/sendcomm",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> sendCommEvent(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) SendNodesData nodes, HttpServletResponse response) throws IOException {
        if (nodes == null || !nodes.isDataReady() || !nodes.isTypeReady()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed (either token or data is missing)");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            return distributedRpcService.sendRemoteCommEvent(token, nodes.getNodes(), nodes.getType(), nodes.getData());
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/restart",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> restartSupervisors(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (!validator.isAdmin(claims)) {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need ADMINISTRATIVE privileges to perform this command!");
                return null;
            }
            return distributedRpcService.restartSupervisors(token, nodes.getNodes());
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/clear/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> clearProcessLogs(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, @PathVariable String name, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return distributedRpcService.clearProcessLogs(token, nodes.getNodes(), name);
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/update",
        method = RequestMethod.POST
    )
    public Map<String, ResultStatus> update(@RequestHeader(value = "Authorization", required = false) String token, @RequestBody(required = false) RequestNodes nodes, HttpServletResponse response) throws IOException {
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            return distributedRpcService.update(token, nodes.getNodes());
        }
        return null;
    }

    @RequestMapping(
        value = "/api/nodes/tail/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultTailLog> getTailLogs(@RequestBody(required = false) RequestNodes nodes, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
        // no validation of token, this is essentially a GET method, but it is simply easier to use the same POST as with authenticated methods, for the client
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
            return null;
        }
        return distributedRpcService.tailProcessStdoutLog(nodes.getNodes(), name, offset, length);
    }

    @RequestMapping(
        value = "/api/nodes/tail/err/{name}",
        method = RequestMethod.POST
    )
    public Map<String, ResultTailLog> getTailErrLogs(@RequestBody(required = false) RequestNodes nodes, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
        // no validation of token, this is essentially a GET method, but it is simply easier to use the same POST as with authenticated methods, for the client
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
            return null;
        }
        return distributedRpcService.tailProcessStderrLog(nodes.getNodes(), name, offset, length);
    }
}
