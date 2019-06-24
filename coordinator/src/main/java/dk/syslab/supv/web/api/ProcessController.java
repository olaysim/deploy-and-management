package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.FileRpcService;
import dk.syslab.supv.rpc.XmlRpcService;
import dk.syslab.supv.rpc.model.FileUuidBytes;
import dk.syslab.supv.storage.FileService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.FineUploaderProgram;
import dk.syslab.supv.web.api.model.Program;
import dk.syslab.supv.web.api.model.Result;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
public class ProcessController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    XmlRpcService xmlRpcService;

    @Autowired
    FileRpcService fileRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @Autowired
    Validator validator;

    @Autowired
    FileService fileService;

    @RequestMapping(
        value = "/api/{node}/process",
        method = RequestMethod.POST
    )
    public Result uploadProcess(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @ModelAttribute Program program, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                if (!validator.isAdmin(claims) && (!validator.validateBannedName(claims.getSubject()) || !validator.validateBannedName(program.getName()))) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name or username is invalid or not allowed!");
                    return null;
                }
                if (!validator.validateName(program.getName())) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name contains invalid characters");
                    return null;
                }

                // if no command was supplied, this "has" to be an update, so check if program exists
//                if (program.getCommand() == null || program.getCommand().isEmpty()) {
//                    // check if program exists
//                    if (fileService.programExists(claims.getSubject(), program.getName())) {
//                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed or trying to update a non-existing process!");
//                        return null;
//                    }
//                }

                String programUuid = UUID.randomUUID().toString();
                FineUploaderProgram fineProgram = fileService.saveProgramToTemporaryLocation(program, programUuid);
                List<FileUuidBytes> fileUuidBytes = fileService.generateFileUuidBytesList(fineProgram);
                fileRpcService.uploadFiles(target, token, fileUuidBytes);
                fileRpcService.uploadProgram(target, token, fineProgram);

                return new Result(true);
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
        }
        return new Result(false, "not authenticated");
    }

    @RequestMapping(
        value = "/api/{node}/process/{name}",
        method = RequestMethod.DELETE
    )
    public Result deleteProcess(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node,  @PathVariable String name, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
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
            try {
                boolean res = false;
                try {
                    res = xmlRpcService.stopProcess(target, token, name, true);
                } catch (IOException ex) {
                    if (ex.getMessage().contains("NOT_RUNNING")) {
                        res = true;
                    } else {
                        throw ex;
                    }
                }
                if (!res) {
                    response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to stop program (please check status manually)");
                    return null;
                }
                fileRpcService.deleteProgram(target, token, name);
                return new Result(true);
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to delete program", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            } catch (IllegalArgumentException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
        }
        return new Result(false, "not authenticated");
    }

    @RequestMapping(
        value = "/api/{node}/process/{name}",
        method = RequestMethod.POST
    )
    public List<String> listProgramFiles(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No program name was supplied");
            return null;
        }
        if (!validator.validateName(name)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name contains invalid characters");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                return fileRpcService.listProgramFiles(target, token, name);
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/configuration/{name}",
        method = RequestMethod.GET
    )
    public Program getProgramConfiguration(@PathVariable String node, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (name == null || name.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No program name was supplied");
            return null;
        }
        if (!validator.validateName(name)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name contains invalid characters");
            return null;
        }
        try {
            return fileRpcService.readProgramConfiguration(target, name);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return null;
        }
    }
}
