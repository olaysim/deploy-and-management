package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.FileRpcService;
import dk.syslab.supv.storage.FileService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.Group;
import dk.syslab.supv.web.api.model.Result;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class GroupController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    Validator validator;

    @Autowired
    FileService fileService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @Autowired
    FileRpcService fileRpcService;

    @RequestMapping(
        value = "/api/{node}/group/{group}/{name}",
        method = RequestMethod.POST
    )
    public Result addProcessToGroup(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node,  @PathVariable String group, @PathVariable String name, @RequestParam(required = false) Integer priority, HttpServletResponse response) throws IOException {
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
                if (!validator.isAdmin(claims) && (!validator.validateBannedName(group) || !validator.validateBannedName(name))) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Program name or group name is invalid or not allowed!");
                    return null;
                }
                int prio = 999; // default
                if (priority != null) {
                    prio = priority;
                }
                fileRpcService.addProgramToGroup(target, token, group, name, prio);
                return new Result(true);
            } catch (IOException e) {
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return new Result(false, "not authenticated");
    }

    @RequestMapping(
        value = "/api/{node}/group/{group}/{name}",
        method = RequestMethod.DELETE
    )
    public Result deleteProcessFromGroup(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @PathVariable String group, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (name == null || name.isEmpty() || group == null || group.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No program name or group name was supplied");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                fileRpcService.removeProgramFromGroup(target, token, group, name);
                return new Result(true);
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return new Result(false, "not authenticated");
    }

    @RequestMapping(
        value = "/api/{node}/group",
        method = RequestMethod.GET
    )
    public List<Group> listGroups(@PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        try {
            List<Group> groups = new ArrayList<>();
            for (Map.Entry<String, List<String>> entry : fileRpcService.listGroups(target).entrySet()) {
                Group group = new Group();
                group.setName(entry.getKey());
                group.setPrograms(entry.getValue());
                groups.add(group);
            }
            return groups;
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }
}
