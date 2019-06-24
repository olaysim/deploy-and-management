package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.XmlRpcService;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;
import dk.syslab.supv.rpc.model.xmlrpc.TailLog;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.Log;
import dk.syslab.supv.web.api.model.Result;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class LogController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    XmlRpcService xmlRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @Autowired
    Validator validator;

    @RequestMapping(
        value = "/api/{node}/log",
        method = RequestMethod.GET
    )
    public Log getMainLog(@PathVariable String node, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
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
            return xmlRpcService.readMainLog(target, offset, length);
        } catch (IOException e) {
            log.error("unable to read log", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }

    @RequestMapping(
        value = "/api/{node}/log/{name}",
        method = RequestMethod.GET
    )
    public Log getLog(@PathVariable String node, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
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
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return xmlRpcService.readProcessStdoutLog(target, name, offset, length);
        } catch (IOException e) {
            if (e.getMessage().contains("BAD_NAME")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
            log.error("unable to read log for process: " + name, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }

    @RequestMapping(
        value = "/api/{node}/log/err/{name}",
        method = RequestMethod.GET
    )
    public Log getErrLog(@PathVariable String node, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
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
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return xmlRpcService.readProcessStderrLog(target, name, offset, length);
        } catch (IOException e) {
            if (e.getMessage().contains("BAD_NAME")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
            log.error("unable to read error log for process: " + name, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }

    @RequestMapping(
        value = "/api/{node}/tail/{name}",
        method = RequestMethod.GET
    )
    public TailLog getTailLog(@PathVariable String node, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
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
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return xmlRpcService.tailProcessStdoutLog(target, name, offset, length);
        } catch (IOException e) {
            if (e.getMessage().contains("BAD_NAME")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
            log.error("unable to tail log for process: " + name, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }

    @RequestMapping(
        value = "/api/{node}/tail/err/{name}",
        method = RequestMethod.GET
    )
    public TailLog getTailErrLog(@PathVariable String node, @PathVariable String name, @RequestParam int offset, @RequestParam int length, HttpServletResponse response) throws IOException {
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
            if (name == null || name.isEmpty()) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                return null;
            }
            return xmlRpcService.tailProcessStderrLog(target, name, offset, length);
        } catch (IOException e) {
            if (e.getMessage().contains("BAD_NAME")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
            log.error("unable to tail error log for process: " + name, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }


    @RequestMapping(
        value = "/api/{node}/log/clear/{name}",
        method = RequestMethod.POST
    )
    public Result clearProcessLog(@PathVariable String node, @RequestHeader(value = "Authorization", required = false) String token, @PathVariable String name, HttpServletResponse response) throws IOException {
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
                if (name == null || name.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                    return null;
                }
                return new Result(xmlRpcService.clearProcessLogs(target, token, name));
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to clear log for: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/log/clear/all",
        method = RequestMethod.POST
    )
    public List<ProcessStatus> clearAllProcessLogs(@PathVariable String node, @RequestHeader(value = "Authorization", required = false) String token, HttpServletResponse response) throws IOException {
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
                return xmlRpcService.clearAllProcessLogs(target, token);
            } catch (IOException e) {
                log.error("unable to clear all logs", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }
}
