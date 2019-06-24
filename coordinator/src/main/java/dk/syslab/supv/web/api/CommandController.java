package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.XmlRpcService;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessStatus;
import dk.syslab.supv.web.Validator;
import dk.syslab.supv.web.api.model.Result;
import dk.syslab.supv.web.api.model.SendData;
import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class CommandController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    XmlRpcService xmlRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @Autowired
    Validator validator;

    @RequestMapping(
        value = "/api/{node}/start/{name}",
        method = RequestMethod.POST
    )
    public Result startProcess(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestParam(required = false) Integer wait, @PathVariable String name, HttpServletResponse response) throws IOException {
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
                return new Result(xmlRpcService.startProcess(target, token, name, (wait != null && wait == 1)));
            } catch (IOException e) {
                if (e.getMessage().contains("ALREADY_STARTED")) return new Result(false, e.getMessage());
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to start process: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/start/group/{name}",
        method = RequestMethod.POST
    )
    public List<ProcessStatus> startProcessGroup(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestParam(required = false) Integer wait, @PathVariable String name, HttpServletResponse response) throws IOException {
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
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
                    return null;
                }
                return xmlRpcService.startProcessGroup(target, token, name, (wait != null && wait == 1));
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to start process group: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/stop/{name}",
        method = RequestMethod.POST
    )
    public Result stopProcess(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestParam(required = false) Integer wait, @PathVariable String name, HttpServletResponse response) throws IOException {
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
                return new Result(xmlRpcService.stopProcess(target, token, name, (wait != null && wait == 1)));
            } catch (IOException e) {
                if (e.getMessage().contains("NOT_RUNNING")) return new Result(false, e.getMessage());
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to stop process: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/stop/group/{name}",
        method = RequestMethod.POST
    )
    public List<ProcessStatus> stopProcessGroup(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestParam(required = false) Integer wait, @PathVariable String name, HttpServletResponse response) throws IOException {
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
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
                    return null;
                }
                return xmlRpcService.stopProcessGroup(target, token, name, (wait != null && wait == 1));
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to stop process group: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/signal/{name}/{signal}",
        method = RequestMethod.POST
    )
    public Result signalProcess(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @PathVariable String name, @PathVariable String signal, HttpServletResponse response) throws IOException {
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
                if (signal == null || signal.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No signal type supplied");
                    return null;
                }
                return new Result(xmlRpcService.signalProcess(target, token, name, signal));
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                if (e.getMessage().contains("BAD_SIGNAL")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to signal process: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/signal/all/{signal}",
        method = RequestMethod.POST
    )
    public List<ProcessStatus> signalAllProcesses(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @PathVariable String signal, HttpServletResponse response) throws IOException {
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
                if (signal == null || signal.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No signal type supplied");
                    return null;
                }
                return xmlRpcService.signalAllProcesses(target, token, signal);
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_SIGNAL")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to signal all processes", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/signal/group/{name}/{signal}",
        method = RequestMethod.POST
    )
    public List<ProcessStatus> signalProcessGroup(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @PathVariable String name, @PathVariable String signal, HttpServletResponse response) throws IOException {
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
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No group name supplied");
                    return null;
                }
                if (signal == null || signal.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No signal type supplied");
                    return null;
                }
                return xmlRpcService.signalProcessGroup(target, token, name, signal);
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                if (e.getMessage().contains("BAD_SIGNAL")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to signal process group: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
            value = "/api/{node}/update",
            method = RequestMethod.POST
    )
    public Result update(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, HttpServletResponse response) throws IOException {
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
                return new Result(xmlRpcService.update(target, token));
            } catch (IOException e) {
                log.error("unable to reload configuration", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/send/{name}",
        method = RequestMethod.POST
    )
    public Result sendProcessStdin(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestBody(required = false) SendData sendData, @PathVariable String name, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (sendData == null || !sendData.isDataReady()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed (data is missing)");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                if (name == null || name.isEmpty()) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process or group name supplied");
                    return null;
                }
                return new Result(xmlRpcService.sendProcessStdin(target, token, name, sendData.getData()));
            } catch (IOException e) {
                if (e.getMessage().contains("BAD_NAME")) {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                    return null;
                }
                log.error("unable to send data to stdin for process: " + name, e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }

    @RequestMapping(
        value = "/api/{node}/sendcomm",
        method = RequestMethod.POST
    )
    public Result sendRemoteCommEvent(@RequestHeader(value = "Authorization", required = false) String token, @PathVariable String node, @RequestBody(required = false) SendData sendData, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        if (sendData == null || !sendData.isDataReady() || !sendData.isTypeReady()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Requestbody is malformed (either token, data or type is missing)");
            return null;
        }
        Claims claims = validator.validate(token, response);
        if (claims != null) {
            try {
                return new Result(xmlRpcService.sendRemoteCommEvent(target, token, sendData.getType(), sendData.getData()));
            } catch (IOException e) {
                log.error("unable to send remote comm event", e);
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
                return null;
            }
        }
        return null;
    }
}
