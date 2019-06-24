package dk.syslab.supv.web.api;

import dk.syslab.supv.rpc.BroadcastRpcService;
import dk.syslab.supv.rpc.StatisticsRpcService;
import dk.syslab.supv.rpc.model.statistics.ProcessData;
import dk.syslab.supv.rpc.model.statistics.ProcessStatistics;
import dk.syslab.supv.rpc.model.statistics.SystemInformation;
import dk.syslab.supv.rpc.model.statistics.SystemStatistics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

@RestController
public class StatisticsController {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    @Autowired
    StatisticsRpcService statisticsRpcService;

    @Autowired
    BroadcastRpcService broadcastRpcService;

    @RequestMapping(
        value = "/api/{node}/stats",
        method = RequestMethod.GET
    )
    public SystemStatistics getSystemStatistics(@PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        return statisticsRpcService.getSystemStatistics(target);
    }

    @RequestMapping(
        value = "/api/{node}/stats/info",
        method = RequestMethod.GET
    )
    public SystemInformation getSystemInformation(@PathVariable String node, HttpServletResponse response) throws IOException {
        if (node == null || node.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No node host address supplied!");
            return null;
        }
        String target = broadcastRpcService.lookUpAddress(node);
        if (target == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unable to lookup address for node host!");
            return null;
        }
        return statisticsRpcService.getSystemInformation(target);
    }

    @RequestMapping(
        value = "/api/{node}/stats/{name}",
        method = RequestMethod.GET
    )
    public HashMap<String, ProcessData> getProcessStatistics(@PathVariable String node, @PathVariable String name, HttpServletResponse response) throws IOException {
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
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No process name supplied");
            return null;
        }
        ProcessStatistics processStatistics = statisticsRpcService.getProcessStatistics(target, name);
        HashMap<String, ProcessData> map = new HashMap<>();
        map.put("weeks", processStatistics.getWeeks().get(name));
        map.put("hour", processStatistics.getHour().get(name));
        return map;
    }
}
