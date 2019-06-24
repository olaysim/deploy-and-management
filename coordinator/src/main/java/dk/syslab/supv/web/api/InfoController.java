package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.XmlRpcService;
import dk.syslab.supv.rpc.model.xmlrpc.ProcessInfo;
import dk.syslab.supv.rpc.model.xmlrpc.SupervisorInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@RestController
public class InfoController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    XmlRpcService xmlRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @RequestMapping(
        value = "/api/{node}/info",
        method = RequestMethod.GET
    )
    public SupervisorInfo getSupervisorInfo(@PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        return xmlRpcService.getSupervisorInfo(target);
    }

    @RequestMapping(
        value = "/api/{node}/info/{name}",
        method = RequestMethod.GET
    )
    public ProcessInfo getProcessInfo(@PathVariable String name, @PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No target node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        try {
            return xmlRpcService.getProcessInfo(target, name);
        } catch (IOException e) {
            if (e.getMessage().contains("BAD_NAME")) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
                return null;
            }
            log.error("unable to get processinfo for process: " + name, e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }

    @RequestMapping(
        value = "/api/{node}/info/all",
        method = RequestMethod.GET
    )
    public List<ProcessInfo> getAllProcessInfo(@PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No target node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        try {
            return xmlRpcService.getAllProcessInfo(target);
        } catch (IOException e) {
            log.error("unable to get process info for ALL", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
            return null;
        }
    }
}
