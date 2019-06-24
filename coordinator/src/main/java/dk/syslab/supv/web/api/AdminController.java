package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.XmlRpcService;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.Result;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class AdminController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    XmlRpcService xmlRpcService;

    @Autowired
    Validator validator;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @RequestMapping(
        value = "/api/{node}/admin/shutdown",
        method = RequestMethod.POST
    )
    public Result stopSupervisor(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No target node host address supplied!");
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
                if (!validator.isAdmin(claims)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need ADMINISTRATIVE privileges to perform this command!");
                    return null;
                }
                return new Result(xmlRpcService.shutdown(target, token));
            } catch (IOException e) {
                log.error("unable to shutdown supervisor", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/admin/restart",
        method = RequestMethod.POST
    )
    public Result restartSupervisor(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, HttpServletResponse response) throws IOException {
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
                if (!validator.isAdmin(claims)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need ADMINISTRATIVE privileges to perform this command!");
                    return null;
                }
                return new Result(xmlRpcService.restart(target, token));
            } catch (IOException e) {
                log.error("unable to restart supervisor", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/admin/clearlog",
        method = RequestMethod.POST
    )
    public Result clearLog(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, HttpServletResponse response) throws IOException {
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
                if (!validator.isAdmin(claims)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need ADMINISTRATIVE privileges to perform this command!");
                    return null;
                }
                return new Result(xmlRpcService.clearMainLog(target, token));
            } catch (IOException e) {
                log.error("unable to clear log", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
            value = "/api/{node}/admin/reload",
            method = RequestMethod.POST
    )
    public Map<String, List<String>> reload(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, HttpServletResponse response) throws IOException {
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
                if (!validator.isAdmin(claims)) {
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "You need ADMINISTRATIVE privileges to perform this command!");
                    return null;
                }
                return xmlRpcService.reloadConfig(target, token);
            } catch (IOException e) {
                log.error("unable to reload configuration", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }
}
